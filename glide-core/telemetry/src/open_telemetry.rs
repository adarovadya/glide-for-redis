use opentelemetry::global::ObjectSafeSpan;
use opentelemetry::trace::{SpanKind, TraceContextExt, TraceError};
use opentelemetry::{global, trace::Tracer};
use opentelemetry_otlp::{MetricExporter, Protocol, WithExportConfig};
use opentelemetry_sdk::export::trace::SpanExporter;
use opentelemetry_sdk::metrics::{MetricError, SdkMeterProvider};
use opentelemetry_sdk::propagation::TraceContextPropagator;
use opentelemetry_sdk::runtime::Tokio;
use opentelemetry_sdk::trace::{BatchConfig, BatchSpanProcessor, TracerProvider};
use std::io::{Error, ErrorKind};
use std::path::PathBuf;
use std::sync::{Arc, Mutex, RwLock};
use thiserror::Error;
use url::Url;

const SPAN_WRITE_LOCK_ERR: &str = "Failed to acquire span write lock";
const SPAN_READ_LOCK_ERR: &str = "Failed to acquire span read lock";
const TRACE_SCOPE: &str = "valkey_glide";

// Metric names
const TIMEOUT_ERROR_METRIC: &str = "glide.timeout_errors";

/// Custom error type for OpenTelemetry errors in Glide
#[derive(Debug, Error)]
pub enum GlideOTELError {
    #[error("Glide OpenTelemetry trace error: {0}")]
    OpenTelemetry(#[from] TraceError),

    #[error("Glide OpenTelemetry metric error: {0}")]
    MetricError(#[from] MetricError),

    #[error("Failed to acquire span read lock")]
    SpanReadLockError,

    #[error("Failed to acquire span write lock")]
    SpanWriteLockError,

    #[error("Other error: {0}")]
    Other(String),
}

/// Default interval in milliseconds for flushing open telemetry data to the collector.
pub const DEFAULT_FLUSH_SPAN_INTERVAL_MS: u64 = 5000;

pub enum GlideSpanStatus {
    Ok,
    Error(String),
}

#[allow(dead_code)]
#[derive(Clone, Debug)]
/// Defines the method that exporter connects to the collector. It can be:
/// gRPC or HTTP. The third type (i.e. "File") defines an exporter that does not connect to a collector
/// instead, it writes the collected signals to files.
pub enum GlideOpenTelemetrySignalsExporter {
    /// Collector is listening on grpc
    Grpc(String),
    /// Collector is listening on http
    Http(String),
    /// No collector. Instead, write the signals collected to a file. The contained value "PathBuf"
    /// points to the folder where the collected data should be placed.
    File(PathBuf),
}

impl std::str::FromStr for GlideOpenTelemetrySignalsExporter {
    type Err = Error;
    fn from_str(s: &str) -> Result<Self, Self::Err> {
        parse_endpoint(s)
    }
}

fn parse_endpoint(endpoint: &str) -> Result<GlideOpenTelemetrySignalsExporter, Error> {
    // Parse the URL using the `url` crate to validate it
    let url = Url::parse(endpoint)
        .map_err(|_| Error::new(ErrorKind::InvalidInput, format!("Parse error. {endpoint}")))?;

    match url.scheme() {
        "http" => Ok(GlideOpenTelemetrySignalsExporter::Http(format!(
            "{}:{}",
            url.host_str().unwrap_or("127.0.0.1"),
            url.port().unwrap_or(80)
        ))), // HTTP endpoint
        "https" => Ok(GlideOpenTelemetrySignalsExporter::Http(format!(
            "{}:{}",
            url.host_str().unwrap_or("127.0.0.1"),
            url.port().unwrap_or(443)
        ))), // HTTPS endpoint
        "grpc" => Ok(GlideOpenTelemetrySignalsExporter::Grpc(format!(
            "{}:{}",
            url.host_str().unwrap_or("127.0.0.1"),
            url.port().unwrap_or(80)
        ))), // gRPC endpoint
        _ => Err(Error::new(ErrorKind::InvalidInput, endpoint)),
    }
}

#[derive(Clone, Debug)]
struct GlideSpanInner {
    span: Arc<RwLock<opentelemetry::global::BoxedSpan>>,
}

impl GlideSpanInner {
    /// Create new span with no parent.
    pub fn new(name: &str) -> Self {
        let tracer = global::tracer(TRACE_SCOPE);
        let span = Arc::new(RwLock::new(
            tracer
                .span_builder(name.to_string())
                .with_kind(SpanKind::Client)
                .start(&tracer),
        ));
        GlideSpanInner { span }
    }

    /// Create new span as a child of `parent`.
    pub fn new_with_parent(name: &str, parent: &GlideSpanInner) -> Self {
        let parent_span_ctx = parent
            .span
            .read()
            .expect(SPAN_READ_LOCK_ERR)
            .span_context()
            .clone();

        let parent_context =
            opentelemetry::Context::new().with_remote_span_context(parent_span_ctx);

        let tracer = global::tracer(TRACE_SCOPE);
        let span = Arc::new(RwLock::new(
            tracer
                .span_builder(name.to_string())
                .with_kind(SpanKind::Client)
                .start_with_context(&tracer, &parent_context),
        ));
        GlideSpanInner { span }
    }

    /// Attach event with name and list of attributes to this span.
    pub fn add_event(&self, name: &str, attributes: Option<&Vec<(&str, &str)>>) {
        let attributes: Vec<opentelemetry::KeyValue> = if let Some(attributes) = attributes {
            attributes
                .iter()
                .map(|(k, v)| opentelemetry::KeyValue::new(k.to_string(), v.to_string()))
                .collect()
        } else {
            Vec::<opentelemetry::KeyValue>::default()
        };
        self.span
            .write()
            .expect(SPAN_WRITE_LOCK_ERR)
            .add_event_with_timestamp(
                name.to_string().into(),
                std::time::SystemTime::now(),
                attributes,
            );
    }

    pub fn set_status(&self, status: GlideSpanStatus) {
        match status {
            GlideSpanStatus::Ok => self
                .span
                .write()
                .expect(SPAN_WRITE_LOCK_ERR)
                .set_status(opentelemetry::trace::Status::Ok),
            GlideSpanStatus::Error(what) => {
                self.span.write().expect(SPAN_WRITE_LOCK_ERR).set_status(
                    opentelemetry::trace::Status::Error {
                        description: what.into(),
                    },
                )
            }
        }
    }

    /// Create new span, add it as a child to this span and return it
    pub fn add_span(&self, name: &str) -> GlideSpanInner {
        let child = GlideSpanInner::new_with_parent(name, self);
        {
            let child_span = child.span.read().expect(SPAN_WRITE_LOCK_ERR);
            self.span
                .write()
                .expect(SPAN_WRITE_LOCK_ERR)
                .add_link(child_span.span_context().clone(), Vec::default());
        }
        child
    }

    /// Return the span ID
    pub fn id(&self) -> String {
        self.span
            .read()
            .expect(SPAN_READ_LOCK_ERR)
            .span_context()
            .span_id()
            .to_string()
    }

    /// Finishes the `Span`.
    pub fn end(&self) {
        self.span.write().expect(SPAN_READ_LOCK_ERR).end()
    }
}

#[derive(Clone, Debug)]
pub struct GlideSpan {
    inner: GlideSpanInner,
}

impl GlideSpan {
    pub fn new(name: &str) -> Self {
        GlideSpan {
            inner: GlideSpanInner::new(name),
        }
    }

    /// Attach event with name to this span.
    pub fn add_event(&self, name: &str) {
        self.inner.add_event(name, None)
    }

    /// Attach event with name and attributes to this span.
    pub fn add_event_with_attributes(&self, name: &str, attributes: &Vec<(&str, &str)>) {
        self.inner.add_event(name, Some(attributes))
    }

    pub fn set_status(&self, status: GlideSpanStatus) {
        self.inner.set_status(status)
    }

    /// Add child span to this span and return it
    pub fn add_span(&self, name: &str) -> GlideSpan {
        GlideSpan {
            inner: self.inner.add_span(name),
        }
    }

    pub fn id(&self) -> String {
        self.inner.id()
    }

    /// Finishes the `Span`.
    pub fn end(&self) {
        self.inner.end()
    }
}

/// OpenTelemetry configuration object. Use `GlideOpenTelemetryConfigBuilder` to construct it:
///
/// ```text
/// let config = GlideOpenTelemetryConfigBuilder::default()
///    .with_flush_interval(std::time::Duration::from_millis(100))
///    .build();
/// GlideOpenTelemetry::initialise(config);
/// ```
#[derive(Clone, Debug)]
pub struct GlideOpenTelemetryConfig {
    /// Default delay interval between two consecutive exports.
    flush_interval_ms: std::time::Duration,
    /// Determines the protocol between the collector and GLIDE for traces
    trace_exporter: GlideOpenTelemetrySignalsExporter,
    /// Determines the protocol between the collector and GLIDE for metrics
    metrics_exporter: GlideOpenTelemetrySignalsExporter,
}

#[derive(Clone, Debug)]
#[allow(dead_code)]
pub struct GlideOpenTelemetryConfigBuilder {
    flush_interval_ms: std::time::Duration,
    trace_exporter: GlideOpenTelemetrySignalsExporter,
    metrics_exporter: GlideOpenTelemetrySignalsExporter,
}

impl Default for GlideOpenTelemetryConfigBuilder {
    fn default() -> Self {
        GlideOpenTelemetryConfigBuilder {
            flush_interval_ms: std::time::Duration::from_millis(DEFAULT_FLUSH_SPAN_INTERVAL_MS),
            trace_exporter: GlideOpenTelemetrySignalsExporter::File(std::env::temp_dir()),
            metrics_exporter: GlideOpenTelemetrySignalsExporter::File(std::env::temp_dir()),
        }
    }
}

impl GlideOpenTelemetryConfigBuilder {
    pub fn with_flush_interval(mut self, duration: std::time::Duration) -> Self {
        self.flush_interval_ms = duration;
        self
    }

    pub fn with_trace_exporter(mut self, protocol: GlideOpenTelemetrySignalsExporter) -> Self {
        self.trace_exporter = protocol;
        self
    }

    pub fn with_metrics_exporter(mut self, protocol: GlideOpenTelemetrySignalsExporter) -> Self {
        self.metrics_exporter = protocol;
        self
    }

    pub fn build(self) -> GlideOpenTelemetryConfig {
        GlideOpenTelemetryConfig {
            flush_interval_ms: self.flush_interval_ms,
            trace_exporter: self.trace_exporter,
            metrics_exporter: self.metrics_exporter,
        }
    }
}

fn build_exporter(
    batch_config: BatchConfig,
    exporter: impl SpanExporter + 'static,
) -> BatchSpanProcessor<Tokio> {
    BatchSpanProcessor::builder(exporter, Tokio)
        .with_batch_config(batch_config)
        .build()
}

#[derive(Clone)]
pub struct GlideOpenTelemetry {}

static TIMEOUT_COUNTER: Mutex<Option<opentelemetry::metrics::Counter<u64>>> = Mutex::new(None);

/// Our interface to OpenTelemetry
impl GlideOpenTelemetry {
    /// Initialise the open telemetry library with a file system exporter
    ///
    /// This method should be called once for the given **process**
    pub fn initialise(config: GlideOpenTelemetryConfig) -> Result<(), GlideOTELError> {
        // Initialize trace exporter
        Self::initialise_trace_exporter(config.flush_interval_ms, &config.trace_exporter)?;

        // Initialize metrics exporter
        Self::initialise_metrics_exporter(config.flush_interval_ms, &config.metrics_exporter)?;

        // Initialize metrics
        Self::init_metrics()?;

        Ok(())
    }

    /// Initialize the trace exporter based on the configuration
    fn initialise_trace_exporter(
        flush_interval_ms: std::time::Duration,
        trace_exporter: &GlideOpenTelemetrySignalsExporter,
    ) -> Result<(), GlideOTELError> {
        let batch_config = opentelemetry_sdk::trace::BatchConfigBuilder::default()
            .with_scheduled_delay(flush_interval_ms)
            .build();

        let trace_exporter = match trace_exporter {
            GlideOpenTelemetrySignalsExporter::File(p) => {
                let exporter = crate::SpanExporterFile::new(p.clone());
                build_exporter(batch_config, exporter)
            }
            GlideOpenTelemetrySignalsExporter::Http(url) => {
                let exporter = opentelemetry_otlp::SpanExporter::builder()
                    .with_http()
                    .with_endpoint(url)
                    .with_protocol(Protocol::HttpBinary)
                    .build()?;
                build_exporter(batch_config, exporter)
            }
            GlideOpenTelemetrySignalsExporter::Grpc(url) => {
                let exporter = opentelemetry_otlp::SpanExporter::builder()
                    .with_tonic()
                    .with_endpoint(url)
                    .with_protocol(Protocol::Grpc)
                    .build()?;
                build_exporter(batch_config, exporter)
            }
        };

        global::set_text_map_propagator(TraceContextPropagator::new());
        let provider = TracerProvider::builder()
            .with_span_processor(trace_exporter)
            .build();
        global::set_tracer_provider(provider);

        Ok(())
    }

    /// Initialize the metrics exporter based on the configuration
    fn initialise_metrics_exporter(
        flush_interval_ms: std::time::Duration,
        metrics_exporter: &GlideOpenTelemetrySignalsExporter,
    ) -> Result<(), GlideOTELError> {
        let metrics_exporter = match metrics_exporter {
            GlideOpenTelemetrySignalsExporter::File(p) => {
                //     let exporter = crate::MetricsExporterFile::new(p);
                //     opentelemetry_sdk::metrics::PeriodicReader::builder(exporter, Tokio)
                //         .with_interval(flush_interval_ms)
                //         .build()
                // let exporter = MetricExporter::builder()
                //     .with_http()
                //     .with_endpoint("url")
                //     .with_protocol(Protocol::HttpBinary)
                //     .build()?;
                let exporter = crate::SpanExporterFile::new(p.clone());
                opentelemetry_sdk::metrics::PeriodicReader::builder(exporter, Tokio)
                    .with_interval(flush_interval_ms)
                    .build()
            }
            GlideOpenTelemetrySignalsExporter::Http(url) => {
                let exporter = MetricExporter::builder()
                    .with_http()
                    .with_endpoint(url)
                    .with_protocol(Protocol::HttpBinary)
                    .build()?;
                opentelemetry_sdk::metrics::PeriodicReader::builder(exporter, Tokio)
                    .with_interval(flush_interval_ms)
                    .build()
            }
            GlideOpenTelemetrySignalsExporter::Grpc(url) => {
                let exporter = MetricExporter::builder()
                    .with_tonic()
                    .with_endpoint(url)
                    .with_protocol(Protocol::Grpc)
                    .build()?;
                opentelemetry_sdk::metrics::PeriodicReader::builder(exporter, Tokio)
                    .with_interval(flush_interval_ms)
                    .build()
            }
        };

        let meter_provider = SdkMeterProvider::builder()
            .with_reader(metrics_exporter)
            .build();
        global::set_meter_provider(meter_provider);

        Ok(())
    }

    /// Initialize metrics counters
    fn init_metrics() -> Result<(), GlideOTELError> {
        // Create the meter and counter
        let meter = global::meter(TRACE_SCOPE);
        TIMEOUT_COUNTER
            .lock()
            .map_err(|_| GlideOTELError::Other("Failed to initialize timeout counter".to_string()))?
            .replace(
                meter
                    .u64_counter(TIMEOUT_ERROR_METRIC)
                    .with_description("Number of timeout errors encountered")
                    .build(),
            );

        Ok(())
    }

    /// Record a timeout error
    pub fn record_timeout_error() -> Result<(), GlideOTELError> {
        TIMEOUT_COUNTER
            .lock()
            .map_err(|_| {
                GlideOTELError::Other("Failed to acquire timeout counter lock".to_string())
            })?
            .as_mut()
            .ok_or_else(|| GlideOTELError::Other("Timeout counter not initialized".to_string()))?
            .add(1, &[]);
        println!("Recorded timeout eror ------------------------");
        Ok(())
    }

    pub fn get_span_interval(config: GlideOpenTelemetryConfig) -> u64 {
        config.flush_interval_ms.as_millis() as u64
    }

    /// Create new span
    pub fn new_span(name: &str) -> GlideSpan {
        GlideSpan::new(name)
    }

    /// Trigger a shutdown procedure flushing all remaining traces
    pub fn shutdown() {
        global::shutdown_tracer_provider();
    }
}

#[cfg(test)]
mod tests {
    use super::*;
    const SPANS_JSON: &str = "/tmp/spans.json";

    fn string_property_to_u64(json: &serde_json::Value, prop: &str) -> u64 {
        let s = json[prop].to_string().replace('"', "");
        s.parse::<u64>().unwrap()
    }

    async fn create_test_spans() {
        let span = GlideOpenTelemetry::new_span("Root_Span_1");
        span.add_event("Event1");
        span.set_status(GlideSpanStatus::Ok);

        let child1 = span.add_span("Network_Span");

        // Simulate some work
        tokio::time::sleep(tokio::time::Duration::from_millis(100)).await;
        child1.end();

        // Simulate that the parent span is still doing some work
        tokio::time::sleep(tokio::time::Duration::from_millis(100)).await;
        span.end();

        let span = GlideOpenTelemetry::new_span("Root_Span_2");
        span.add_event("Event1");
        span.add_event("Event2");
        span.set_status(GlideSpanStatus::Ok);
        drop(span); // writes the span

        tokio::time::sleep(tokio::time::Duration::from_secs(1)).await;
    }

    #[test]
    fn test_span_json_exporter() {
        let _ = std::fs::remove_file(SPANS_JSON);
        let runtime = tokio::runtime::Builder::new_current_thread()
            .enable_all()
            .build()
            .unwrap();
        runtime.block_on(async {
            let config = GlideOpenTelemetryConfigBuilder::default()
                .with_flush_interval(std::time::Duration::from_millis(100))
                .with_trace_exporter(GlideOpenTelemetrySignalsExporter::File(PathBuf::from(
                    "/tmp",
                )))
                .build();
            let _ = GlideOpenTelemetry::initialise(config);
            create_test_spans().await;

            // Read the file content
            let file_content = std::fs::read_to_string(SPANS_JSON).unwrap();
            let lines: Vec<&str> = file_content.split('\n').collect();
            assert_eq!(lines.len(), 4);

            let span_json: serde_json::Value = serde_json::from_str(lines[0]).unwrap();
            assert_eq!(span_json["name"], "Network_Span");
            let network_span_id = span_json["span_id"].to_string();
            let network_span_start_time = string_property_to_u64(&span_json, "start_time");
            let network_span_end_time = string_property_to_u64(&span_json, "end_time");

            // Because of the sleep above, the network span should be at least 100ms (units are microseconds)
            assert!(network_span_end_time - network_span_start_time >= 100_000);

            let span_json: serde_json::Value = serde_json::from_str(lines[1]).unwrap();
            assert_eq!(span_json["name"], "Root_Span_1");
            assert_eq!(span_json["links"].as_array().unwrap().len(), 1); // we expect 1 child
            let root_1_span_start_time = string_property_to_u64(&span_json, "start_time");
            let root_1_span_end_time = string_property_to_u64(&span_json, "end_time");

            // The network span started *after* its parent
            assert!(network_span_start_time >= root_1_span_start_time);

            // The parent span ends *after* the child span (by at least 100ms)
            assert!(root_1_span_end_time - network_span_end_time >= 100_000);

            let child_span_id = span_json["links"][0]["span_id"].to_string();
            assert_eq!(child_span_id, network_span_id);

            let span_json: serde_json::Value = serde_json::from_str(lines[2]).unwrap();
            assert_eq!(span_json["name"], "Root_Span_2");
        });
    }

    #[test]
    fn test_span_http_exporter() {
        let runtime = tokio::runtime::Builder::new_current_thread()
            .enable_all()
            .build()
            .unwrap();
        runtime.block_on(async {
            let config = GlideOpenTelemetryConfigBuilder::default()
                .with_flush_interval(std::time::Duration::from_millis(100))
                .with_trace_exporter(GlideOpenTelemetrySignalsExporter::Http(
                    "http://test.com".to_string(),
                ))
                .build();
            let _ = GlideOpenTelemetry::initialise(config);
            create_test_spans().await;
        });
    }

    #[test]
    fn test_span_grpc_exporter() {
        let runtime = tokio::runtime::Builder::new_current_thread()
            .enable_all()
            .build()
            .unwrap();
        runtime.block_on(async {
            let config = GlideOpenTelemetryConfigBuilder::default()
                .with_flush_interval(std::time::Duration::from_millis(100))
                .with_trace_exporter(GlideOpenTelemetrySignalsExporter::Grpc(
                    "grpc://test.com".to_string(),
                ))
                .build();
            let _ = GlideOpenTelemetry::initialise(config);
            create_test_spans().await;
        });
    }

    #[test]
    fn test_record_timeout_error() {
        let runtime = tokio::runtime::Builder::new_current_thread()
            .enable_all()
            .build()
            .unwrap();
        runtime.block_on(async {
            let config = GlideOpenTelemetryConfigBuilder::default()
                .with_flush_interval(std::time::Duration::from_millis(100))
                .with_trace_exporter(GlideOpenTelemetrySignalsExporter::File(PathBuf::from(
                    "/tmp",
                )))
                .build();
            let _ = GlideOpenTelemetry::initialise(config);
            GlideOpenTelemetry::record_timeout_error().unwrap();
        });
    }
}
