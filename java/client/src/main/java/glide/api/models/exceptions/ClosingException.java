/** Copyright Valkey GLIDE Project Contributors - SPDX Identifier: Apache-2.0 */
package glide.api.models.exceptions;

/** Closed client error: Errors that report that the client has closed and is no longer usable. */
public class ClosingException extends GlideException {
    public ClosingException(String message) {
        super(message);
    }
}
