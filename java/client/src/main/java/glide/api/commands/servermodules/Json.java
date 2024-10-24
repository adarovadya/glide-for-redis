/** Copyright Valkey GLIDE Project Contributors - SPDX Identifier: Apache-2.0 */
package glide.api.commands.servermodules;

import static glide.api.models.GlideString.gs;
import static glide.utils.ArrayTransformUtils.concatenateArrays;

import glide.api.BaseClient;
import glide.api.GlideClient;
import glide.api.GlideClusterClient;
import glide.api.models.ClusterValue;
import glide.api.models.GlideString;
import glide.api.models.commands.ConditionalChange;
import glide.api.models.commands.json.JsonGetOptions;
import glide.api.models.commands.json.JsonGetOptionsBinary;
import glide.utils.ArgsBuilder;
import java.util.concurrent.CompletableFuture;
import lombok.NonNull;

/** Module for JSON commands. */
public class Json {

    private static final String JSON_PREFIX = "JSON.";
    private static final String JSON_SET = JSON_PREFIX + "SET";
    private static final String JSON_GET = JSON_PREFIX + "GET";
    private static final String JSON_ARRAPPEND = JSON_PREFIX + "ARRAPPEND";
    private static final String JSON_ARRINSERT = JSON_PREFIX + "ARRINSERT";
    private static final String JSON_ARRLEN = JSON_PREFIX + "ARRLEN";
    private static final String JSON_OBJLEN = JSON_PREFIX + "OBJLEN";
    private static final String JSON_OBJKEYS = JSON_PREFIX + "OBJKEYS";
    private static final String JSON_DEL = JSON_PREFIX + "DEL";
    private static final String JSON_FORGET = JSON_PREFIX + "FORGET";
    private static final String JSON_TOGGLE = JSON_PREFIX + "TOGGLE";

    private Json() {}

    /**
     * Sets the JSON value at the specified <code>path</code> stored at <code>key</code>.
     *
     * @param client The Valkey GLIDE client to execute the command.
     * @param key The <code>key</code> of the JSON document.
     * @param path Represents the path within the JSON document where the value will be set. The key
     *     will be modified only if <code>value</code> is added as the last child in the specified
     *     <code>path</code>, or if the specified <code>path</code> acts as the parent of a new child
     *     being added.
     * @param value The value to set at the specific path, in JSON formatted string.
     * @return A simple <code>"OK"</code> response if the value is successfully set.
     * @example
     *     <pre>{@code
     * String value = Json.set(client, "doc", ".", "{\"a\": 1.0, \"b\": 2}").get();
     * assert value.equals("OK");
     * }</pre>
     */
    public static CompletableFuture<String> set(
            @NonNull BaseClient client,
            @NonNull String key,
            @NonNull String path,
            @NonNull String value) {
        return executeCommand(client, new String[] {JSON_SET, key, path, value});
    }

    /**
     * Sets the JSON value at the specified <code>path</code> stored at <code>key</code>.
     *
     * @param client The Valkey GLIDE client to execute the command.
     * @param key The <code>key</code> of the JSON document.
     * @param path Represents the path within the JSON document where the value will be set. The key
     *     will be modified only if <code>value</code> is added as the last child in the specified
     *     <code>path</code>, or if the specified <code>path</code> acts as the parent of a new child
     *     being added.
     * @param value The value to set at the specific path, in JSON formatted GlideString.
     * @return A simple <code>"OK"</code> response if the value is successfully set.
     * @example
     *     <pre>{@code
     * String value = Json.set(client, gs("doc"), gs("."), gs("{\"a\": 1.0, \"b\": 2}")).get();
     * assert value.equals("OK");
     * }</pre>
     */
    public static CompletableFuture<String> set(
            @NonNull BaseClient client,
            @NonNull GlideString key,
            @NonNull GlideString path,
            @NonNull GlideString value) {
        return executeCommand(client, new GlideString[] {gs(JSON_SET), key, path, value});
    }

    /**
     * Sets the JSON value at the specified <code>path</code> stored at <code>key</code>.
     *
     * @param client The Valkey GLIDE client to execute the command.
     * @param key The <code>key</code> of the JSON document.
     * @param path Represents the path within the JSON document where the value will be set. The key
     *     will be modified only if <code>value</code> is added as the last child in the specified
     *     <code>path</code>, or if the specified <code>path</code> acts as the parent of a new child
     *     being added.
     * @param value The value to set at the specific path, in JSON formatted string.
     * @param setCondition Set the value only if the given condition is met (within the key or path).
     * @return A simple <code>"OK"</code> response if the value is successfully set. If value isn't
     *     set because of <code>setCondition</code>, returns <code>null</code>.
     * @example
     *     <pre>{@code
     * String value = Json.set(client, "doc", ".", "{\"a\": 1.0, \"b\": 2}", ConditionalChange.ONLY_IF_DOES_NOT_EXIST).get();
     * assert value.equals("OK");
     * }</pre>
     */
    public static CompletableFuture<String> set(
            @NonNull BaseClient client,
            @NonNull String key,
            @NonNull String path,
            @NonNull String value,
            @NonNull ConditionalChange setCondition) {
        return executeCommand(
                client, new String[] {JSON_SET, key, path, value, setCondition.getValkeyApi()});
    }

    /**
     * Sets the JSON value at the specified <code>path</code> stored at <code>key</code>.
     *
     * @param client The Valkey GLIDE client to execute the command.
     * @param key The <code>key</code> of the JSON document.
     * @param path Represents the path within the JSON document where the value will be set. The key
     *     will be modified only if <code>value</code> is added as the last child in the specified
     *     <code>path</code>, or if the specified <code>path</code> acts as the parent of a new child
     *     being added.
     * @param value The value to set at the specific path, in JSON formatted GlideString.
     * @param setCondition Set the value only if the given condition is met (within the key or path).
     * @return A simple <code>"OK"</code> response if the value is successfully set. If value isn't
     *     set because of <code>setCondition</code>, returns <code>null</code>.
     * @example
     *     <pre>{@code
     * String value = Json.set(client, gs("doc"), gs("."), gs("{\"a\": 1.0, \"b\": 2}"), ConditionalChange.ONLY_IF_DOES_NOT_EXIST).get();
     * assert value.equals("OK");
     * }</pre>
     */
    public static CompletableFuture<String> set(
            @NonNull BaseClient client,
            @NonNull GlideString key,
            @NonNull GlideString path,
            @NonNull GlideString value,
            @NonNull ConditionalChange setCondition) {
        return executeCommand(
                client,
                new GlideString[] {gs(JSON_SET), key, path, value, gs(setCondition.getValkeyApi())});
    }

    /**
     * Retrieves the JSON value at the specified <code>path</code> stored at <code>key</code>.
     *
     * @param client The Valkey GLIDE client to execute the command.
     * @param key The <code>key</code> of the JSON document.
     * @return Returns a string representation of the JSON document. If <code>key</code> doesn't
     *     exist, returns <code>null</code>.
     * @example
     *     <pre>{@code
     * String value = Json.get(client, "doc").get();
     * assert value.equals("{\"a\": 1.0, \"b\": 2}");
     * }</pre>
     */
    public static CompletableFuture<String> get(@NonNull BaseClient client, @NonNull String key) {
        return executeCommand(client, new String[] {JSON_GET, key});
    }

    /**
     * Retrieves the JSON value at the specified <code>path</code> stored at <code>key</code>.
     *
     * @param client The Valkey GLIDE client to execute the command.
     * @param key The <code>key</code> of the JSON document.
     * @return Returns a string representation of the JSON document. If <code>key</code> doesn't
     *     exist, returns <code>null</code>.
     * @example
     *     <pre>{@code
     * GlideString value = Json.get(client, gs("doc")).get();
     * assert value.equals(gs("{\"a\": 1.0, \"b\": 2}"));
     * }</pre>
     */
    public static CompletableFuture<GlideString> get(
            @NonNull BaseClient client, @NonNull GlideString key) {
        return executeCommand(client, new GlideString[] {gs(JSON_GET), key});
    }

    /**
     * Retrieves the JSON value at the specified <code>paths</code> stored at <code>key</code>.
     *
     * @param client The Valkey GLIDE client to execute the command.
     * @param key The <code>key</code> of the JSON document.
     * @param paths List of paths within the JSON document.
     * @return
     *     <ul>
     *       <li>If one path is given:
     *           <ul>
     *             <li>For JSONPath (path starts with <code>$</code>): Returns a stringified JSON list
     *                 replies for every possible path, or a string representation of an empty array,
     *                 if path doesn't exist. If <code>key</code> doesn't exist, returns None.
     *             <li>For legacy path (path doesn't start with <code>$</code>): Returns a string
     *                 representation of the value in <code>paths</code>. If <code>paths</code>
     *                 doesn't exist, an error is raised. If <code>key</code> doesn't exist, returns
     *                 None.
     *           </ul>
     *       <li>If multiple paths are given: Returns a stringified JSON, in which each path is a key,
     *           and it's corresponding value, is the value as if the path was executed in the command
     *           as a single path.
     *     </ul>
     *     In case of multiple paths, and <code>paths</code> are a mix of both JSONPath and legacy
     *     path, the command behaves as if all are JSONPath paths.
     * @example
     *     <pre>{@code
     * String value = Json.get(client, "doc", new String[] {"$"}).get();
     * assert value.equals("{\"a\": 1.0, \"b\": 2}");
     * String value = Json.get(client, "doc", new String[] {"$.a", "$.b"}).get();
     * assert value.equals("{\"$.a\": [1.0], \"$.b\": [2]}");
     * }</pre>
     */
    public static CompletableFuture<String> get(
            @NonNull BaseClient client, @NonNull String key, @NonNull String[] paths) {
        return executeCommand(client, concatenateArrays(new String[] {JSON_GET, key}, paths));
    }

    /**
     * Retrieves the JSON value at the specified <code>paths</code> stored at <code>key</code>.
     *
     * @param client The Valkey GLIDE client to execute the command.
     * @param key The <code>key</code> of the JSON document.
     * @param paths List of paths within the JSON document.
     * @return
     *     <ul>
     *       <li>If one path is given:
     *           <ul>
     *             <li>For JSONPath (path starts with <code>$</code>): Returns a stringified JSON list
     *                 replies for every possible path, or a string representation of an empty array,
     *                 if path doesn't exist. If <code>key</code> doesn't exist, returns None.
     *             <li>For legacy path (path doesn't start with <code>$</code>): Returns a string
     *                 representation of the value in <code>paths</code>. If <code>paths</code>
     *                 doesn't exist, an error is raised. If <code>key</code> doesn't exist, returns
     *                 None.
     *           </ul>
     *       <li>If multiple paths are given: Returns a stringified JSON, in which each path is a key,
     *           and it's corresponding value, is the value as if the path was executed in the command
     *           as a single path.
     *     </ul>
     *     In case of multiple paths, and <code>paths</code> are a mix of both JSONPath and legacy
     *     path, the command behaves as if all are JSONPath paths.
     * @example
     *     <pre>{@code
     * GlideString value = Json.get(client, gs("doc"), new GlideString[] {gs("$")}).get();
     * assert value.equals(gs("{\"a\": 1.0, \"b\": 2}"));
     * GlideString value = Json.get(client, gs("doc"), new GlideString[] {gs("$.a"), gs("$.b")}).get();
     * assert value.equals(gs("{\"$.a\": [1.0], \"$.b\": [2]}"));
     * }</pre>
     */
    public static CompletableFuture<GlideString> get(
            @NonNull BaseClient client, @NonNull GlideString key, @NonNull GlideString[] paths) {
        return executeCommand(client, concatenateArrays(new GlideString[] {gs(JSON_GET), key}, paths));
    }

    /**
     * Retrieves the JSON value at the specified <code>path</code> stored at <code>key</code>.
     *
     * @param client The Valkey GLIDE client to execute the command.
     * @param key The <code>key</code> of the JSON document.
     * @param options Options for formatting the byte representation of the JSON data. See <code>
     *     JsonGetOptions</code>.
     * @return Returns a string representation of the JSON document. If <code>key</code> doesn't
     *     exist, returns <code>null</code>.
     * @example
     *     <pre>{@code
     * JsonGetOptions options = JsonGetOptions.builder()
     *                              .indent("  ")
     *                              .space(" ")
     *                              .newline("\n")
     *                              .build();
     * String value = Json.get(client, "doc", "$", options).get();
     * assert value.equals("{\n \"a\": \n  1.0\n ,\n \"b\": \n  2\n }");
     * }</pre>
     */
    public static CompletableFuture<String> get(
            @NonNull BaseClient client, @NonNull String key, @NonNull JsonGetOptions options) {
        return executeCommand(
                client, concatenateArrays(new String[] {JSON_GET, key}, options.toArgs()));
    }

    /**
     * Retrieves the JSON value at the specified <code>path</code> stored at <code>key</code>.
     *
     * @param client The Valkey GLIDE client to execute the command.
     * @param key The <code>key</code> of the JSON document.
     * @param options Options for formatting the byte representation of the JSON data. See <code>
     *     JsonGetOptions</code>.
     * @return Returns a string representation of the JSON document. If <code>key</code> doesn't
     *     exist, returns <code>null</code>.
     * @example
     *     <pre>{@code
     * JsonGetOptions options = JsonGetOptions.builder()
     *                              .indent("  ")
     *                              .space(" ")
     *                              .newline("\n")
     *                              .build();
     * GlideString value = Json.get(client, gs("doc"), gs("$"), options).get();
     * assert value.equals(gs("{\n \"a\": \n  1.0\n ,\n \"b\": \n  2\n }"));
     * }</pre>
     */
    public static CompletableFuture<GlideString> get(
            @NonNull BaseClient client, @NonNull GlideString key, @NonNull JsonGetOptionsBinary options) {
        return executeCommand(
                client, new ArgsBuilder().add(gs(JSON_GET)).add(key).add(options.toArgs()).toArray());
    }

    /**
     * Retrieves the JSON value at the specified <code>path</code> stored at <code>key</code>.
     *
     * @param client The Valkey GLIDE client to execute the command.
     * @param key The <code>key</code> of the JSON document.
     * @param paths List of paths within the JSON document.
     * @param options Options for formatting the byte representation of the JSON data. See <code>
     *     JsonGetOptions</code>.
     * @return
     *     <ul>
     *       <li>If one path is given:
     *           <ul>
     *             <li>For JSONPath (path starts with <code>$</code>): Returns a stringified JSON list
     *                 replies for every possible path, or a string representation of an empty array,
     *                 if path doesn't exist. If <code>key</code> doesn't exist, returns None.
     *             <li>For legacy path (path doesn't start with <code>$</code>): Returns a string
     *                 representation of the value in <code>paths</code>. If <code>paths</code>
     *                 doesn't exist, an error is raised. If <code>key</code> doesn't exist, returns
     *                 None.
     *           </ul>
     *       <li>If multiple paths are given: Returns a stringified JSON, in which each path is a key,
     *           and it's corresponding value, is the value as if the path was executed in the command
     *           as a single path.
     *     </ul>
     *     In case of multiple paths, and <code>paths</code> are a mix of both JSONPath and legacy
     *     path, the command behaves as if all are JSONPath paths.
     * @example
     *     <pre>{@code
     * JsonGetOptions options = JsonGetOptions.builder()
     *                              .indent("  ")
     *                              .space(" ")
     *                              .newline("\n")
     *                              .build();
     * String value = Json.get(client, "doc", new String[] {"$.a", "$.b"}, options).get();
     * assert value.equals("{\n \"$.a\": [\n  1.0\n ],\n \"$.b\": [\n  2\n ]\n}");
     * }</pre>
     */
    public static CompletableFuture<String> get(
            @NonNull BaseClient client,
            @NonNull String key,
            @NonNull String[] paths,
            @NonNull JsonGetOptions options) {
        return executeCommand(
                client, concatenateArrays(new String[] {JSON_GET, key}, options.toArgs(), paths));
    }

    /**
     * Retrieves the JSON value at the specified <code>path</code> stored at <code>key</code>.
     *
     * @param client The Valkey GLIDE client to execute the command.
     * @param key The <code>key</code> of the JSON document.
     * @param paths List of paths within the JSON document.
     * @param options Options for formatting the byte representation of the JSON data. See <code>
     *     JsonGetOptions</code>.
     * @return
     *     <ul>
     *       <li>If one path is given:
     *           <ul>
     *             <li>For JSONPath (path starts with <code>$</code>): Returns a stringified JSON list
     *                 replies for every possible path, or a string representation of an empty array,
     *                 if path doesn't exist. If <code>key</code> doesn't exist, returns None.
     *             <li>For legacy path (path doesn't start with <code>$</code>): Returns a string
     *                 representation of the value in <code>paths</code>. If <code>paths</code>
     *                 doesn't exist, an error is raised. If <code>key</code> doesn't exist, returns
     *                 None.
     *           </ul>
     *       <li>If multiple paths are given: Returns a stringified JSON, in which each path is a key,
     *           and it's corresponding value, is the value as if the path was executed in the command
     *           as a single path.
     *     </ul>
     *     In case of multiple paths, and <code>paths</code> are a mix of both JSONPath and legacy
     *     path, the command behaves as if all are JSONPath paths.
     * @example
     *     <pre>{@code
     * JsonGetOptions options = JsonGetOptions.builder()
     *                              .indent("  ")
     *                              .space(" ")
     *                              .newline("\n")
     *                              .build();
     * GlideString value = Json.get(client, gs("doc"), new GlideString[] {gs("$.a"), gs("$.b")}, options).get();
     * assert value.equals(gs("{\n \"$.a\": [\n  1.0\n ],\n \"$.b\": [\n  2\n ]\n}"));
     * }</pre>
     */
    public static CompletableFuture<GlideString> get(
            @NonNull BaseClient client,
            @NonNull GlideString key,
            @NonNull GlideString[] paths,
            @NonNull JsonGetOptionsBinary options) {
        return executeCommand(
                client,
                new ArgsBuilder().add(gs(JSON_GET)).add(key).add(options.toArgs()).add(paths).toArray());
    }

    /**
     * Appends one or more <code>values</code> to the JSON array at the specified <code>path</code>
     * within the JSON document stored at <code>key</code>.
     *
     * @param client The client to execute the command.
     * @param key The <code>key</code> of the JSON document.
     * @param path Represents the <code>path</code> within the JSON document where the <code>values
     *     </code> will be appended.
     * @param values The <code>values</code> to append to the JSON array at the specified <code>path
     *     </code>.
     * @return
     *     <ul>
     *       <li>For JSONPath (<code>path</code> starts with <code>$</code>):<br>
     *           Returns a list of integers for every possible path, indicating the new length of the
     *           new array after appending <code>values</code>, or <code>null</code> for JSON values
     *           matching the path that are not an array. If <code>path</code> does not exist, an
     *           empty array will be returned.
     *       <li>For legacy path (<code>path</code> doesn't start with <code>$</code>):<br>
     *           Returns the length of the new array after appending <code>values</code> to the array
     *           at <code>path</code>. If multiple paths are matched, returns the last updated array.
     *           If the JSON value at <code>path</code> is not a array or if <code>path</code> doesn't
     *           exist, an error is raised. If <code>key</code> doesn't exist, an error is raised.
     * @example
     *     <pre>{@code
     * Json.set(client, "doc", "$", "{\"a\": 1, \"b\": [\"one\", \"two\"]}").get();
     * var res = Json.arrappend(client, "doc", "$.b", new String[] {"\"three\""}).get();
     * assert Arrays.equals((Object[]) res, new int[] {3}); // New length of the array after appending
     * res = Json.arrappend(client, "doc", ".b", new String[] {"\"four\""}).get();
     * assert res.equals(4); // New length of the array after appending
     * }</pre>
     */
    public static CompletableFuture<Object> arrappend(
            @NonNull BaseClient client,
            @NonNull String key,
            @NonNull String path,
            @NonNull String[] values) {
        return executeCommand(
                client, concatenateArrays(new String[] {JSON_ARRAPPEND, key, path}, values));
    }

    /**
     * Appends one or more <code>values</code> to the JSON array at the specified <code>path</code>
     * within the JSON document stored at <code>key</code>.
     *
     * @param client The client to execute the command.
     * @param key The <code>key</code> of the JSON document.
     * @param path Represents the <code>path</code> within the JSON document where the <code>values
     *     </code> will be appended.
     * @param values The <code>values</code> to append to the JSON array at the specified <code>path
     *     </code>.
     * @return
     *     <ul>
     *       <li>For JSONPath (<code>path</code> starts with <code>$</code>):<br>
     *           Returns a list of integers for every possible path, indicating the new length of the
     *           new array after appending <code>values</code>, or <code>null</code> for JSON values
     *           matching the path that are not an array. If <code>path</code> does not exist, an
     *           empty array will be returned.
     *       <li>For legacy path (<code>path</code> doesn't start with <code>$</code>):<br>
     *           Returns the length of the new array after appending <code>values</code> to the array
     *           at <code>path</code>. If multiple paths are matched, returns the last updated array.
     *           If the JSON value at <code>path</code> is not a array or if <code>path</code> doesn't
     *           exist, an error is raised. If <code>key</code> doesn't exist, an error is raised.
     * @example
     *     <pre>{@code
     * Json.set(client, "doc", "$", "{\"a\": 1, \"b\": [\"one\", \"two\"]}").get();
     * var res = Json.arrappend(client, gs("doc"), gs("$.b"), new GlideString[] {gs("\"three\"")}).get();
     * assert Arrays.equals((Object[]) res, new int[] {3}); // New length of the array after appending
     * res = Json.arrappend(client, gs("doc"), gs(".b"), new GlideString[] {gs("\"four\"")}).get();
     * assert res.equals(4); // New length of the array after appending
     * }</pre>
     */
    public static CompletableFuture<Object> arrappend(
            @NonNull BaseClient client,
            @NonNull GlideString key,
            @NonNull GlideString path,
            @NonNull GlideString[] values) {
        return executeCommand(
                client, new ArgsBuilder().add(gs(JSON_ARRAPPEND)).add(key).add(path).add(values).toArray());
    }

    /**
     * Inserts one or more values into the array at the specified <code>path</code> within the JSON
     * document stored at <code>key</code>, before the given <code>index</code>.
     *
     * @param client The client to execute the command.
     * @param key The key of the JSON document.
     * @param path The path within the JSON document.
     * @param index The array index before which values are inserted.
     * @param values The JSON values to be inserted into the array, in JSON formatted bytes or str.
     *     JSON string values must be wrapped with quotes. For example, to append <code>"foo"</code>,
     *     pass <code>"\"foo\""</code>.
     * @return
     *     <ul>
     *       <li>For JSONPath (<code>path</code> starts with <code>$</code>):<br>
     *           Returns an <code>Object[]</code> with a list of integers for every possible path,
     *           indicating the new length of the array, or <code>null</code> for JSON values matching
     *           the path that are not an array. If <code>path</code> does not exist, an empty array
     *           will be returned.
     *       <li>For legacy path (<code>path</code> doesn't start with <code>$</code>):<br>
     *           Returns an integer representing the new length of the array. If multiple paths are
     *           matched, returns the length of the first modified array. If <code>path</code> doesn't
     *           exist or the value at <code>path</code> is not an array, an error is raised.
     *     </ul>
     *     If the index is out of bounds or <code>key</code> doesn't exist, an error is raised.
     * @example
     *     <pre>{@code
     * Json.set(client, "doc", "$", "[[], [\"a\"], [\"a\", \"b\"]]").get();
     * var newValues = new String[] { "\"c\"", "{\"key\": \"value\"}", "true", "null", "[\"bar\"]" };
     * var res = Json.arrinsert(client, "doc", "$[*]", 0, newValues).get();
     * assert Arrays.equals((Object[]) res, new int[] { 5, 6, 7 }); // New lengths of arrays after insertion
     * var doc = Json.get(client, "doc").get();
     * assert doc.equals("[[\"c\", {\"key\": \"value\"}, true, null, [\"bar\"]], [\"c\", {\"key\": \"value\"}, "
     *     + "true, null, [\"bar\"], \"a\"], [\"c\", {\"key\": \"value\"}, true, null, [\"bar\"], \"a\", \"b\"]]");
     *
     * Json.set(client, "doc", "$", "[[], [\"a\"], [\"a\", \"b\"]]").get();
     * res = Json.arrinsert(client, "doc", ".", 0, new String[] { "\"c\"" }).get();
     * assert res == 4 // New length of the root array after insertion
     * doc = Json.get(client, "doc").get();
     * assert doc.equals("[\"c\", [], [\"a\"], [\"a\", \"b\"]]");
     * }</pre>
     */
    public static CompletableFuture<Object> arrinsert(
            @NonNull BaseClient client,
            @NonNull String key,
            @NonNull String path,
            int index,
            @NonNull String[] values) {
        return executeCommand(
                client,
                concatenateArrays(
                        new String[] {JSON_ARRINSERT, key, path, Integer.toString(index)}, values));
    }

    /**
     * Inserts one or more values into the array at the specified <code>path</code> within the JSON
     * document stored at <code>key</code>, before the given <code>index</code>.
     *
     * @param client The client to execute the command.
     * @param key The key of the JSON document.
     * @param path The path within the JSON document.
     * @param index The array index before which values are inserted.
     * @param values The JSON values to be inserted into the array, in JSON formatted bytes or str.
     *     JSON string values must be wrapped with quotes. For example, to append <code>"foo"</code>,
     *     pass <code>"\"foo\""</code>.
     * @return
     *     <ul>
     *       <li>For JSONPath (<code>path</code> starts with <code>$</code>):<br>
     *           Returns an <code>Object[]</code> with a list of integers for every possible path,
     *           indicating the new length of the array, or <code>null</code> for JSON values matching
     *           the path that are not an array. If <code>path</code> does not exist, an empty array
     *           will be returned.
     *       <li>For legacy path (<code>path</code> doesn't start with <code>$</code>):<br>
     *           Returns an integer representing the new length of the array. If multiple paths are
     *           matched, returns the length of the first modified array. If <code>path</code> doesn't
     *           exist or the value at <code>path</code> is not an array, an error is raised.
     *     </ul>
     *     If the index is out of bounds or <code>key</code> doesn't exist, an error is raised.
     * @example
     *     <pre>{@code
     * Json.set(client, "doc", "$", "[[], [\"a\"], [\"a\", \"b\"]]").get();
     * var newValues = new GlideString[] { gs("\"c\""), gs("{\"key\": \"value\"}"), gs("true"), gs("null"), gs("[\"bar\"]") };
     * var res = Json.arrinsert(client, gs("doc"), gs("$[*]"), 0, newValues).get();
     * assert Arrays.equals((Object[]) res, new int[] { 5, 6, 7 }); // New lengths of arrays after insertion
     * var doc = Json.get(client, "doc").get();
     * assert doc.equals("[[\"c\", {\"key\": \"value\"}, true, null, [\"bar\"]], [\"c\", {\"key\": \"value\"}, "
     *     + "true, null, [\"bar\"], \"a\"], [\"c\", {\"key\": \"value\"}, true, null, [\"bar\"], \"a\", \"b\"]]");
     *
     * Json.set(client, "doc", "$", "[[], [\"a\"], [\"a\", \"b\"]]").get();
     * res = Json.arrinsert(client, gs("doc"), gs("."), 0, new GlideString[] { gs("\"c\"") }).get();
     * assert res == 4 // New length of the root array after insertion
     * doc = Json.get(client, "doc").get();
     * assert doc.equals("[\"c\", [], [\"a\"], [\"a\", \"b\"]]");
     * }</pre>
     */
    public static CompletableFuture<Object> arrinsert(
            @NonNull BaseClient client,
            @NonNull GlideString key,
            @NonNull GlideString path,
            int index,
            @NonNull GlideString[] values) {
        return executeCommand(
                client,
                new ArgsBuilder()
                        .add(gs(JSON_ARRINSERT))
                        .add(key)
                        .add(path)
                        .add(Integer.toString(index))
                        .add(values)
                        .toArray());
    }

    /**
     * Retrieves the length of the array at the specified <code>path</code> within the JSON document
     * stored at <code>key</code>.
     *
     * @param client The client to execute the command.
     * @param key The key of the JSON document.
     * @param path The path within the JSON document.
     * @return
     *     <ul>
     *       <li>For JSONPath (<code>path</code> starts with <code>$</code>):<br>
     *           Returns an <code>Object[]</code> with a list of integers for every possible path,
     *           indicating the length of the array, or <code>null</code> for JSON values matching the
     *           path that are not an array. If <code>path</code> does not exist, an empty array will
     *           be returned.
     *       <li>For legacy path (<code>path</code> doesn't start with <code>$</code>):<br>
     *           Returns an integer representing the length of the array. If multiple paths are
     *           matched, returns the length of the first matching array. If <code>path</code> doesn't
     *           exist or the value at <code>path</code> is not an array, an error is raised.
     *     </ul>
     *     If <code>key</code> doesn't exist, returns <code>null</code>.
     * @example
     *     <pre>{@code
     * Json.set(client, "doc", "$", "{\"a\": [1, 2, 3], \"b\": {\"a\": [1, 2], \"c\": {\"a\": 42}}}").get();
     * var res = Json.arrlen(client, "doc", "$").get();
     * assert Arrays.equals((Object[]) res, new Object[] { null }); // No array at the root path.
     * res = Json.arrlen(client, "doc", "$.a").get();
     * assert Arrays.equals((Object[]) res, new Object[] { 3 }); // Retrieves the length of the array at path $.a.
     * res = Json.arrlen(client, "doc", "$..a").get();
     * assert Arrays.equals((Object[]) res, new Object[] { 3, 2, null }); // Retrieves lengths of arrays found at all levels of the path `..a`.
     * res = Json.arrlen(client, "doc", "..a").get();
     * assert res == 3; // Legacy path retrieves the first array match at path `..a`.
     * }</pre>
     */
    public static CompletableFuture<Object> arrlen(
            @NonNull BaseClient client, @NonNull String key, @NonNull String path) {
        return executeCommand(client, new String[] {JSON_ARRLEN, key, path});
    }

    /**
     * Retrieves the length of the array at the specified <code>path</code> within the JSON document
     * stored at <code>key</code>.
     *
     * @param client The client to execute the command.
     * @param key The key of the JSON document.
     * @param path The path within the JSON document.
     * @return
     *     <ul>
     *       <li>For JSONPath (<code>path</code> starts with <code>$</code>):<br>
     *           Returns an <code>Object[]</code> with a list of integers for every possible path,
     *           indicating the length of the array, or <code>null</code> for JSON values matching the
     *           path that are not an array. If <code>path</code> does not exist, an empty array will
     *           be returned.
     *       <li>For legacy path (<code>path</code> doesn't start with <code>$</code>):<br>
     *           Returns an integer representing the length of the array. If multiple paths are
     *           matched, returns the length of the first matching array. If <code>path</code> doesn't
     *           exist or the value at <code>path</code> is not an array, an error is raised.
     *     </ul>
     *     If <code>key</code> doesn't exist, returns <code>null</code>.
     * @example
     *     <pre>{@code
     * Json.set(client, "doc", "$", "{\"a\": [1, 2, 3], \"b\": {\"a\": [1, 2], \"c\": {\"a\": 42}}}").get();
     * var res = Json.arrlen(client, gs("doc"), gs("$")).get();
     * assert Arrays.equals((Object[]) res, new Object[] { null }); // No array at the root path.
     * res = Json.arrlen(client, gs("doc"), gs("$.a")).get();
     * assert Arrays.equals((Object[]) res, new Object[] { 3 }); // Retrieves the length of the array at path $.a.
     * res = Json.arrlen(client, gs("doc"), gs("$..a")).get();
     * assert Arrays.equals((Object[]) res, new Object[] { 3, 2, null }); // Retrieves lengths of arrays found at all levels of the path `..a`.
     * res = Json.arrlen(client, gs("doc"), gs("..a")).get();
     * assert res == 3; // Legacy path retrieves the first array match at path `..a`.
     * }</pre>
     */
    public static CompletableFuture<Object> arrlen(
            @NonNull BaseClient client, @NonNull GlideString key, @NonNull GlideString path) {
        return executeCommand(client, new GlideString[] {gs(JSON_ARRLEN), key, path});
    }

    /**
     * Retrieves the length of the array at the root of the JSON document stored at <code>key</code>.
     * <br>
     * Equivalent to {@link #arrlen(BaseClient, String, String)} with <code>path</code> set to <code>
     * "."</code>.
     *
     * @param client The client to execute the command.
     * @param key The key of the JSON document.
     * @return The array length stored at the root of the document. If document root is not an array,
     *     an error is raised.<br>
     *     If <code>key</code> doesn't exist, returns <code>null</code>.
     * @example
     *     <pre>{@code
     * Json.set(client, "doc", "$", "[1, 2, true, null, \"tree\"]").get();
     * var res = Json.arrlen(client, "doc").get();
     * assert res == 5;
     * }</pre>
     */
    public static CompletableFuture<Long> arrlen(@NonNull BaseClient client, @NonNull String key) {
        return executeCommand(client, new String[] {JSON_ARRLEN, key});
    }

    /**
     * Retrieves the length of the array at the root of the JSON document stored at <code>key</code>.
     * Equivalent to {@link #arrlen(BaseClient, GlideString, GlideString)} with <code>path</code> set
     * to <code>gs(".")</code>.
     *
     * @param client The client to execute the command.
     * @param key The key of the JSON document.
     * @return The array length stored at the root of the document. If document root is not an array,
     *     an error is raised.<br>
     *     If <code>key</code> doesn't exist, returns <code>null</code>.
     * @example
     *     <pre>{@code
     * Json.set(client, "doc", "$", "[1, 2, true, null, \"tree\"]").get();
     * var res = Json.arrlen(client, gs("doc")).get();
     * assert res == 5;
     * }</pre>
     */
    public static CompletableFuture<Long> arrlen(
            @NonNull BaseClient client, @NonNull GlideString key) {
        return executeCommand(client, new GlideString[] {gs(JSON_ARRLEN), key});
    }

    /**
     * Retrieves the number of key-value pairs in the object values at the specified <code>path</code>
     * within the JSON document stored at <code>key</code>.<br>
     * Equivalent to {@link #objlen(BaseClient, String, String)} with <code>path</code> set to <code>
     * "."</code>.
     *
     * @param client The client to execute the command.
     * @param key The key of the JSON document.
     * @return The object length stored at the root of the document. If document root is not an
     *     object, an error is raised.<br>
     *     If <code>key</code> doesn't exist, returns <code>null</code>.
     * @example
     *     <pre>{@code
     * Json.set(client, "doc", "$", "{\"a\": 1.0, \"b\": {\"a\": {\"x\": 1, \"y\": 2}, \"b\": 2.5, \"c\": true}}").get();
     * var res = Json.objlen(client, "doc").get();
     * assert res == 2; // the size of object matching the path `.`, which has 2 keys: 'a' and 'b'.
     * }</pre>
     */
    public static CompletableFuture<Long> objlen(@NonNull BaseClient client, @NonNull String key) {
        return executeCommand(client, new String[] {JSON_OBJLEN, key});
    }

    /**
     * Retrieves the number of key-value pairs in the object values at the specified <code>path</code>
     * within the JSON document stored at <code>key</code>.<br>
     * Equivalent to {@link #objlen(BaseClient, GlideString, GlideString)} with <code>path</code> set
     * to <code>gs(".")</code>.
     *
     * @param client The client to execute the command.
     * @param key The key of the JSON document.
     * @return The object length stored at the root of the document. If document root is not an
     *     object, an error is raised.<br>
     *     If <code>key</code> doesn't exist, returns <code>null</code>.
     * @example
     *     <pre>{@code
     * Json.set(client, "doc", "$", "{\"a\": 1.0, \"b\": {\"a\": {\"x\": 1, \"y\": 2}, \"b\": 2.5, \"c\": true}}").get();
     * var res = Json.objlen(client, gs("doc"), gs(".")).get();
     * assert res == 2; // the size of object matching the path `.`, which has 2 keys: 'a' and 'b'.
     * }</pre>
     */
    public static CompletableFuture<Long> objlen(
            @NonNull BaseClient client, @NonNull GlideString key) {
        return executeCommand(client, new GlideString[] {gs(JSON_OBJLEN), key});
    }

    /**
     * Retrieves the number of key-value pairs in the object values at the specified <code>path</code>
     * within the JSON document stored at <code>key</code>.
     *
     * @param client The client to execute the command.
     * @param key The key of the JSON document.
     * @param path The path within the JSON document.
     * @return
     *     <ul>
     *       <li>For JSONPath (<code>path</code> starts with <code>$</code>):<br>
     *           Returns an <code>Object[]</code> with a list of long integers for every possible
     *           path, indicating the number of key-value pairs for each matching object, or <code>
     *           null
     *           </code> for JSON values matching the path that are not an object. If <code>path
     *           </code> does not exist, an empty array will be returned.
     *       <li>For legacy path (<code>path</code> doesn't start with <code>$</code>):<br>
     *           Returns the number of key-value pairs for the object value matching the path. If
     *           multiple paths are matched, returns the length of the first matching object. If
     *           <code>path</code> doesn't exist or the value at <code>path</code> is not an array, an
     *           error is raised.
     *     </ul>
     *     If <code>key</code> doesn't exist, returns <code>null</code>.
     * @example
     *     <pre>{@code
     * Json.set(client, "doc", "$", "{\"a\": 1.0, \"b\": {\"a\": {\"x\": 1, \"y\": 2}, \"b\": 2.5, \"c\": true}}").get();
     * var res = Json.objlen(client, "doc", ".").get(); // legacy path - command returns first value as `Long`
     * assert res == 2L; // the size of object matching the path `.`, which has 2 keys: 'a' and 'b'.
     *
     * res = Json.objlen(client, "doc", "$.b").get(); // JSONPath - command returns an array
     * assert Arrays.equals((Object[]) res, new Object[] { 3L }); // the length of the objects at path `$.b`
     * }</pre>
     */
    public static CompletableFuture<Object> objlen(
            @NonNull BaseClient client, @NonNull String key, @NonNull String path) {
        return executeCommand(client, new String[] {JSON_OBJLEN, key, path});
    }

    /**
     * Retrieves the number of key-value pairs in the object values at the specified <code>path</code>
     * within the JSON document stored at <code>key</code>.
     *
     * @param client The client to execute the command.
     * @param key The key of the JSON document.
     * @param path The path within the JSON document.
     * @return
     *     <ul>
     *       <li>For JSONPath (<code>path</code> starts with <code>$</code>):<br>
     *           Returns an <code>Object[]</code> with a list of long integers for every possible
     *           path, indicating the number of key-value pairs for each matching object, or <code>
     *           null
     *           </code> for JSON values matching the path that are not an object. If <code>path
     *           </code> does not exist, an empty array will be returned.
     *       <li>For legacy path (<code>path</code> doesn't start with <code>$</code>):<br>
     *           Returns the number of key-value pairs for the object value matching the path. If
     *           multiple paths are matched, returns the length of the first matching object. If
     *           <code>path</code> doesn't exist or the value at <code>path</code> is not an array, an
     *           error is raised.
     *     </ul>
     *     If <code>key</code> doesn't exist, returns <code>null</code>.
     * @example
     *     <pre>{@code
     * Json.set(client, "doc", "$", "{\"a\": 1.0, \"b\": {\"a\": {\"x\": 1, \"y\": 2}, \"b\": 2.5, \"c\": true}}").get();
     * var res = Json.objlen(client, gs("doc"), gs(".")).get(); // legacy path - command returns first value as `Long`
     * assert res == 2L; // the size of object matching the path `.`, which has 2 keys: 'a' and 'b'.
     *
     * res = Json.objlen(client, gs("doc"), gs("$.b")).get(); // JSONPath - command returns an array
     * assert Arrays.equals((Object[]) res, new Object[] { 3L }); // the length of the objects at path `$.b`
     * }</pre>
     */
    public static CompletableFuture<Object> objlen(
            @NonNull BaseClient client, @NonNull GlideString key, @NonNull GlideString path) {
        return executeCommand(client, new GlideString[] {gs(JSON_OBJLEN), key, path});
    }

    /**
     * Retrieves the key names in the object values at the specified <code>path</code> within the JSON
     * document stored at <code>key</code>.<br>
     * Equivalent to {@link #objkeys(BaseClient, String, String)} with <code>path</code> set to <code>
     * "."</code>.
     *
     * @param client The client to execute the command.
     * @param key The key of the JSON document.
     * @return The object length stored at the root of the document. If document root is not an
     *     object, an error is raised.<br>
     *     If <code>key</code> doesn't exist, returns <code>null</code>.
     * @example
     *     <pre>{@code
     * Json.set(client, "doc", "$", "{\"a\": 1.0, \"b\": {\"a\": {\"x\": 1, \"y\": 2}, \"b\": 2.5, \"c\": true}}").get();
     * var res = Json.objkeys(client, "doc").get();
     * assert Arrays.equals((Object[]) res, new Object[] { "a", "b" }); // the keys of the object matching the path `.`, which has 2 keys: 'a' and 'b'.
     * }</pre>
     */
    public static CompletableFuture<Object[]> objkeys(
            @NonNull BaseClient client, @NonNull String key) {
        return executeCommand(client, new String[] {JSON_OBJKEYS, key});
    }

    /**
     * Retrieves the key names in the object values at the specified <code>path</code> within the JSON
     * document stored at <code>key</code>.<br>
     * Equivalent to {@link #objkeys(BaseClient, GlideString, GlideString)} with <code>path</code> set
     * to <code>gs(".")</code>.
     *
     * @param client The client to execute the command.
     * @param key The key of the JSON document.
     * @return The object length stored at the root of the document. If document root is not an
     *     object, an error is raised.<br>
     *     If <code>key</code> doesn't exist, returns <code>null</code>.
     * @example
     *     <pre>{@code
     * Json.set(client, "doc", "$", "{\"a\": 1.0, \"b\": {\"a\": {\"x\": 1, \"y\": 2}, \"b\": 2.5, \"c\": true}}").get();
     * var res = Json.objkeys(client, gs("doc"), gs(".")).get();
     * assert Arrays.equals((Object[]) res, new Object[] { gs("a"), gs("b") }); // the keys of the object matching the path `.`, which has 2 keys: 'a' and 'b'.
     * }</pre>
     */
    public static CompletableFuture<Object[]> objkeys(
            @NonNull BaseClient client, @NonNull GlideString key) {
        return executeCommand(client, new GlideString[] {gs(JSON_OBJKEYS), key});
    }

    /**
     * Retrieves the key names in the object values at the specified <code>path</code> within the JSON
     * document stored at <code>key</code>.
     *
     * @param client The client to execute the command.
     * @param key The key of the JSON document.
     * @param path The path within the JSON document.
     * @return
     *     <ul>
     *       <li>For JSONPath (<code>path</code> starts with <code>$</code>):<br>
     *           Returns an <code>Object[][]</code> with each nested array containing key names for
     *           each matching object for every possible path, indicating the list of object keys for
     *           each matching object, or <code>null</code> for JSON values matching the path that are
     *           not an object. If <code>path</code> does not exist, an empty sub-array will be
     *           returned.
     *       <li>For legacy path (<code>path</code> doesn't start with <code>$</code>):<br>
     *           Returns an array of object keys for the object value matching the path. If multiple
     *           paths are matched, returns the length of the first matching object. If <code>path
     *           </code> doesn't exist or the value at <code>path</code> is not an array, an error is
     *           raised.
     *     </ul>
     *     If <code>key</code> doesn't exist, returns <code>null</code>.
     * @example
     *     <pre>{@code
     * Json.set(client, "doc", "$", "{\"a\": 1.0, \"b\": {\"a\": {\"x\": 1, \"y\": 2}, \"b\": 2.5, \"c\": true}}").get();
     * var res = Json.objkeys(client, "doc", ".").get(); // legacy path - command returns array for first matched object
     * assert Arrays.equals((Object[]) res, new Object[] { "a", "b" }); // key names for the object matching the path `.` as it is the only match.
     *
     * res = Json.objkeys(client, "doc", "$.b").get(); // JSONPath - command returns an array for each matched object
     * assert Arrays.equals((Object[]) res, new Object[][] { { "a", "b", "c" } }); // key names as a nested list for objects matching the JSONPath `$.b`.
     * }</pre>
     */
    public static CompletableFuture<Object[]> objkeys(
            @NonNull BaseClient client, @NonNull String key, @NonNull String path) {
        return executeCommand(client, new String[] {JSON_OBJKEYS, key, path});
    }

    /**
     * Retrieves the key names in the object values at the specified <code>path</code> within the JSON
     * document stored at <code>key</code>.
     *
     * @param client The client to execute the command.
     * @param key The key of the JSON document.
     * @param path The path within the JSON document.
     * @return
     *     <ul>
     *       <li>For JSONPath (<code>path</code> starts with <code>$</code>):<br>
     *           Returns an <code>Object[][]</code> with each nested array containing key names for
     *           each matching object for every possible path, indicating the list of object keys for
     *           each matching object, or <code>null</code> for JSON values matching the path that are
     *           not an object. If <code>path</code> does not exist, an empty sub-array will be
     *           returned.
     *       <li>For legacy path (<code>path</code> doesn't start with <code>$</code>):<br>
     *           Returns an array of object keys for the object value matching the path. If multiple
     *           paths are matched, returns the length of the first matching object. If <code>path
     *           </code> doesn't exist or the value at <code>path</code> is not an array, an error is
     *           raised.
     *     </ul>
     *     If <code>key</code> doesn't exist, returns <code>null</code>.
     * @example
     *     <pre>{@code
     * Json.set(client, "doc", "$", "{\"a\": 1.0, \"b\": {\"a\": {\"x\": 1, \"y\": 2}, \"b\": 2.5, \"c\": true}}").get();
     * var res = Json.objkeys(client, gs("doc"), gs(".")).get(); // legacy path - command returns array for first matched object
     * assert Arrays.equals((Object[]) res, new Object[] { "a", "b" }); // key names for the object matching the path `.` as it is the only match.
     *
     * res = Json.objkeys(client, gs("doc"), gs("$.b")).get(); // JSONPath - command returns an array for each matched object
     * assert Arrays.equals((Object[]) res, new Object[][] { { "a", "b", "c" } }); // key names as a nested list for objects matching the JSONPath `$.b`.
     * }</pre>
     */
    public static CompletableFuture<Object[]> objkeys(
            @NonNull BaseClient client, @NonNull GlideString key, @NonNull GlideString path) {
        return executeCommand(client, new GlideString[] {gs(JSON_OBJKEYS), key, path});
    }

    /**
     * Deletes the JSON document stored at <code>key</code>.
     *
     * @param client The Valkey GLIDE client to execute the command.
     * @param key The <code>key</code> of the JSON document.
     * @return The number of elements deleted. 0 if the key does not exist.
     * @example
     *     <pre>{@code
     * Json.set(client, "doc", ".", "{\"a\": 1, \"nested\": {\"a\": 2, \"b\": 3}}");
     * Long result = Json.del(client, "doc").get();
     * assert result == 1L;
     * }</pre>
     */
    public static CompletableFuture<Long> del(@NonNull BaseClient client, @NonNull String key) {
        return executeCommand(client, new String[] {JSON_DEL, key});
    }

    /**
     * Deletes the JSON document stored at <code>key</code>.
     *
     * @param client The Valkey GLIDE client to execute the command.
     * @param key The <code>key</code> of the JSON document.
     * @return The number of elements deleted. 0 if the key does not exist.
     * @example
     *     <pre>{@code
     * Json.set(client, "doc", ".", "{\"a\": 1, \"nested\": {\"a\": 2, \"b\": 3}}");
     * Long result = Json.del(client, gs("doc")).get();
     * assert result == 1L;
     * }</pre>
     */
    public static CompletableFuture<Long> del(@NonNull BaseClient client, @NonNull GlideString key) {
        return executeCommand(client, new GlideString[] {gs(JSON_DEL), key});
    }

    /**
     * Deletes the JSON value at the specified <code>path</code> within the JSON document stored at
     * <code>key</code>.
     *
     * @param client The Valkey GLIDE client to execute the command.
     * @param key The <code>key</code> of the JSON document.
     * @param path Represents the path within the JSON document where the value will be deleted.
     * @return The number of elements deleted. 0 if the key does not exist, or if the JSON path is
     *     invalid or does not exist.
     * @example
     *     <pre>{@code
     * Json.set(client, "doc", ".", "{\"a\": 1, \"nested\": {\"a\": 2, \"b\": 3}}");
     * Long result = Json.del(client, "doc", "$..a").get();
     * assert result == 2L;
     * }</pre>
     */
    public static CompletableFuture<Long> del(
            @NonNull BaseClient client, @NonNull String key, @NonNull String path) {
        return executeCommand(client, new String[] {JSON_DEL, key, path});
    }

    /**
     * Deletes the JSON value at the specified <code>path</code> within the JSON document stored at
     * <code>key</code>.
     *
     * @param client The Valkey GLIDE client to execute the command.
     * @param key The <code>key</code> of the JSON document.
     * @param path Represents the path within the JSON document where the value will be deleted.
     * @return The number of elements deleted. 0 if the key does not exist, or if the JSON path is
     *     invalid or does not exist.
     * @example
     *     <pre>{@code
     * Json.set(client, "doc", ".", "{\"a\": 1, \"nested\": {\"a\": 2, \"b\": 3}}");
     * Long result = Json.del(client, gs("doc"), gs("$..a")).get();
     * assert result == 2L;
     * }</pre>
     */
    public static CompletableFuture<Long> del(
            @NonNull BaseClient client, @NonNull GlideString key, @NonNull GlideString path) {
        return executeCommand(client, new GlideString[] {gs(JSON_DEL), key, path});
    }

    /**
     * Deletes the JSON document stored at <code>key</code>.
     *
     * @param client The Valkey GLIDE client to execute the command.
     * @param key The <code>key</code> of the JSON document.
     * @return The number of elements deleted. 0 if the key does not exist.
     * @example
     *     <pre>{@code
     * Json.set(client, "doc", ".", "{\"a\": 1, \"nested\": {\"a\": 2, \"b\": 3}}");
     * Long result = Json.forget(client, "doc").get();
     * assert result == 1L;
     * }</pre>
     */
    public static CompletableFuture<Long> forget(@NonNull BaseClient client, @NonNull String key) {
        return executeCommand(client, new String[] {JSON_FORGET, key});
    }

    /**
     * Deletes the JSON document stored at <code>key</code>.
     *
     * @param client The Valkey GLIDE client to execute the command.
     * @param key The <code>key</code> of the JSON document.
     * @return The number of elements deleted. 0 if the key does not exist.
     * @example
     *     <pre>{@code
     * Json.set(client, "doc", ".", "{\"a\": 1, \"nested\": {\"a\": 2, \"b\": 3}}");
     * Long result = Json.forget(client, gs("doc")).get();
     * assert result == 1L;
     * }</pre>
     */
    public static CompletableFuture<Long> forget(
            @NonNull BaseClient client, @NonNull GlideString key) {
        return executeCommand(client, new GlideString[] {gs(JSON_FORGET), key});
    }

    /**
     * Deletes the JSON value at the specified <code>path</code> within the JSON document stored at
     * <code>key</code>.
     *
     * @param client The Valkey GLIDE client to execute the command.
     * @param key The <code>key</code> of the JSON document.
     * @param path Represents the path within the JSON document where the value will be deleted.
     * @return The number of elements deleted. 0 if the key does not exist, or if the JSON path is
     *     invalid or does not exist.
     * @example
     *     <pre>{@code
     * Json.set(client, "doc", ".", "{\"a\": 1, \"nested\": {\"a\": 2, \"b\": 3}}");
     * Long result = Json.forget(client, "doc", "$..a").get();
     * assert result == 2L;
     * }</pre>
     */
    public static CompletableFuture<Long> forget(
            @NonNull BaseClient client, @NonNull String key, @NonNull String path) {
        return executeCommand(client, new String[] {JSON_FORGET, key, path});
    }

    /**
     * Deletes the JSON value at the specified <code>path</code> within the JSON document stored at
     * <code>key</code>.
     *
     * @param client The Valkey GLIDE client to execute the command.
     * @param key The <code>key</code> of the JSON document.
     * @param path Represents the path within the JSON document where the value will be deleted.
     * @return The number of elements deleted. 0 if the key does not exist, or if the JSON path is
     *     invalid or does not exist.
     * @example
     *     <pre>{@code
     * Json.set(client, "doc", ".", "{\"a\": 1, \"nested\": {\"a\": 2, \"b\": 3}}");
     * Long result = Json.forget(client, gs("doc"), gs("$..a")).get();
     * assert result == 2L;
     * }</pre>
     */
    public static CompletableFuture<Long> forget(
            @NonNull BaseClient client, @NonNull GlideString key, @NonNull GlideString path) {
        return executeCommand(client, new GlideString[] {gs(JSON_FORGET), key, path});
    }

    /**
     * Toggles a Boolean value stored at the root within the JSON document stored at <code>key</code>.
     *
     * @param client The client to execute the command.
     * @param key The key of the JSON document.
     * @return Returns the toggled boolean value at the root of the document, or <code>null</code> for
     *     JSON values matching the root that are not boolean. If <code>key</code> doesn't exist,
     *     returns <code>null</code>.
     * @example
     *     <pre>{@code
     * Json.set(client, "doc", ".", true).get();
     * var res = Json.toggle(client, "doc").get();
     * assert res.equals(false);
     * res = Json.toggle(client, "doc").get();
     * assert res.equals(true);
     * }</pre>
     */
    public static CompletableFuture<Boolean> toggle(@NonNull BaseClient client, @NonNull String key) {
        return executeCommand(client, new String[] {JSON_TOGGLE, key});
    }

    /**
     * Toggles a Boolean value stored at the root within the JSON document stored at <code>key</code>.
     *
     * @param client The client to execute the command.
     * @param key The key of the JSON document.
     * @return Returns the toggled boolean value at the root of the document, or <code>null</code> for
     *     JSON values matching the root that are not boolean. If <code>key</code> doesn't exist,
     *     returns <code>null</code>.
     * @example
     *     <pre>{@code
     * Json.set(client, "doc", ".", true).get();
     * var res = Json.toggle(client, gs("doc")).get();
     * assert res.equals(false);
     * res = Json.toggle(client, gs("doc")).get();
     * assert res.equals(true);
     * }</pre>
     */
    public static CompletableFuture<Boolean> toggle(
            @NonNull BaseClient client, @NonNull GlideString key) {
        return executeCommand(client, new ArgsBuilder().add(gs(JSON_TOGGLE)).add(key).toArray());
    }

    /**
     * Toggles a Boolean value stored at the specified <code>path</code> within the JSON document
     * stored at <code>key</code>.
     *
     * @param client The client to execute the command.
     * @param key The key of the JSON document.
     * @param path The path within the JSON document.
     * @return
     *     <ul>
     *       <li>For JSONPath (<code>path</code> starts with <code>$</code>):<br>
     *           Returns a <code>Boolean[]</code> with the toggled boolean value for every possible
     *           path, or <code>null</code> for JSON values matching the path that are not boolean.
     *       <li>For legacy path (<code>path</code> doesn't start with <code>$</code>):<br>
     *           Returns the value of the toggled boolean in <code>path</code>. If <code>path</code>
     *           doesn't exist or the value at <code>path</code> isn't a boolean, an error is raised.
     *     </ul>
     *     If <code>key</code> doesn't exist, returns <code>null</code>.
     * @example
     *     <pre>{@code
     * Json.set(client, "doc", "$", "{\"bool\": true, \"nested\": {\"bool\": false, \"nested\": {\"bool\": 10}}}").get();
     * var res = Json.toggle(client, "doc", "$..bool").get();
     * assert Arrays.equals((Boolean[]) res, new Boolean[] {false, true, null});
     * res = Json.toggle(client, "doc", "bool").get();
     * assert res.equals(true);
     * var getResult = Json.get(client, "doc", "$").get();
     * assert getResult.equals("{\"bool\": true, \"nested\": {\"bool\": true, \"nested\": {\"bool\": 10}}}");
     * }</pre>
     */
    public static CompletableFuture<Object> toggle(
            @NonNull BaseClient client, @NonNull String key, @NonNull String path) {
        return executeCommand(client, new String[] {JSON_TOGGLE, key, path});
    }

    /**
     * Toggles a Boolean value stored at the specified <code>path</code> within the JSON document
     * stored at <code>key</code>.
     *
     * @param client The client to execute the command.
     * @param key The key of the JSON document.
     * @param path The path within the JSON document.
     * @return
     *     <ul>
     *       <li>For JSONPath (<code>path</code> starts with <code>$</code>):<br>
     *           Returns a <code>Boolean[]</code> with the toggled boolean value for every possible
     *           path, or <code>null</code> for JSON values matching the path that are not boolean.
     *       <li>For legacy path (<code>path</code> doesn't start with <code>$</code>):<br>
     *           Returns the value of the toggled boolean in <code>path</code>. If <code>path</code>
     *           doesn't exist or the value at <code>path</code> isn't a boolean, an error is raised.
     *     </ul>
     *     If <code>key</code> doesn't exist, returns <code>null</code>.
     * @example
     *     <pre>{@code
     * Json.set(client, "doc", "$", "{\"bool\": true, \"nested\": {\"bool\": false, \"nested\": {\"bool\": 10}}}").get();
     * var res = Json.toggle(client, gs("doc"), gs("$..bool")).get();
     * assert Arrays.equals((Boolean[]) res, new Boolean[] {false, true, null});
     * res = Json.toggle(client, gs("doc"), gs("bool")).get();
     * assert res.equals(true);
     * var getResult = Json.get(client, "doc", "$").get();
     * assert getResult.equals("{\"bool\": true, \"nested\": {\"bool\": true, \"nested\": {\"bool\": 10}}}");
     * }</pre>
     */
    public static CompletableFuture<Object> toggle(
            @NonNull BaseClient client, @NonNull GlideString key, @NonNull GlideString path) {
        return executeCommand(
                client, new ArgsBuilder().add(gs(JSON_TOGGLE)).add(key).add(path).toArray());
    }

    /**
     * A wrapper for custom command API.
     *
     * @param client The client to execute the command.
     * @param args The command line.
     */
    private static <T> CompletableFuture<T> executeCommand(BaseClient client, String[] args) {
        return executeCommand(client, args, false);
    }

    /**
     * A wrapper for custom command API.
     *
     * @param client The client to execute the command.
     * @param args The command line.
     * @param returnsMap - true if command returns a map
     */
    @SuppressWarnings({"unchecked", "SameParameterValue"})
    private static <T> CompletableFuture<T> executeCommand(
            BaseClient client, String[] args, boolean returnsMap) {
        if (client instanceof GlideClient) {
            return ((GlideClient) client).customCommand(args).thenApply(r -> (T) r);
        } else if (client instanceof GlideClusterClient) {
            return ((GlideClusterClient) client)
                    .customCommand(args)
                    .thenApply(returnsMap ? ClusterValue::getMultiValue : ClusterValue::getSingleValue)
                    .thenApply(r -> (T) r);
        }
        throw new IllegalArgumentException(
                "Unknown type of client, should be either `GlideClient` or `GlideClusterClient`");
    }

    /**
     * A wrapper for custom command API.
     *
     * @param client The client to execute the command.
     * @param args The command line.
     */
    private static <T> CompletableFuture<T> executeCommand(BaseClient client, GlideString[] args) {
        return executeCommand(client, args, false);
    }

    /**
     * A wrapper for custom command API.
     *
     * @param client The client to execute the command.
     * @param args The command line.
     * @param returnsMap - true if command returns a map
     */
    @SuppressWarnings({"unchecked", "SameParameterValue"})
    private static <T> CompletableFuture<T> executeCommand(
            BaseClient client, GlideString[] args, boolean returnsMap) {
        if (client instanceof GlideClient) {
            return ((GlideClient) client).customCommand(args).thenApply(r -> (T) r);
        } else if (client instanceof GlideClusterClient) {
            return ((GlideClusterClient) client)
                    .customCommand(args)
                    .thenApply(returnsMap ? ClusterValue::getMultiValue : ClusterValue::getSingleValue)
                    .thenApply(r -> (T) r);
        }
        throw new IllegalArgumentException(
                "Unknown type of client, should be either `GlideClient` or `GlideClusterClient`");
    }
}