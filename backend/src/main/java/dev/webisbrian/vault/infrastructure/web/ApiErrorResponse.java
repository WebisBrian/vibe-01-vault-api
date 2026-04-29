package dev.webisbrian.vault.infrastructure.web;

import java.time.Instant;

/**
 * Standard error body returned for every non-2xx response.
 *
 * <p>Defined as a record to guarantee immutability and eliminate boilerplate. All error paths in
 * {@link GlobalExceptionHandler} produce this same shape so API consumers have a single contract
 * to handle regardless of which exception was thrown.
 */
public record ApiErrorResponse(
        int status,
        String error,
        String message,
        Instant timestamp,
        String path
) {}