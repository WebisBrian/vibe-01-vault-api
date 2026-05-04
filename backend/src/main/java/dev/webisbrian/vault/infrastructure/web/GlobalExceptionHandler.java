package dev.webisbrian.vault.infrastructure.web;

import dev.webisbrian.vault.domain.exception.DomainException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.time.Instant;
import java.util.stream.Collectors;

/**
 * Translates exceptions into {@link ApiErrorResponse} HTTP responses.
 *
 * <p>Keeping exception-to-status-code mapping here ensures the domain and application layers
 * remain unaware of HTTP semantics.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Routes domain business exceptions to the appropriate HTTP status using the error code
     * carried by {@link DomainException}. The domain does not know about HTTP — this handler
     * is the single translation point.
     */
    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ApiErrorResponse> handleDomainException(
            DomainException ex, HttpServletRequest request) {
        HttpStatus status = switch (ex.getErrorCode()) {
            case "SECRET_NOT_FOUND" -> HttpStatus.NOT_FOUND;
            case "SECRET_ALREADY_EXISTS" -> HttpStatus.CONFLICT;
            case "INVALID_SECRET" -> HttpStatus.BAD_REQUEST;
            default -> HttpStatus.UNPROCESSABLE_ENTITY;
        };
        logger.warn("Domain exception on {}: [{}] {}", request.getRequestURI(), ex.getErrorCode(), ex.getMessage());
        return build(status, ex.getMessage(), request.getRequestURI());
    }

    /**
     * Collects all field violations into one message so the client receives every constraint
     * failure in a single response, not just the first one Spring stops at.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining(", "));
        logger.warn("Validation failed on {}: {}", request.getRequestURI(), message);
        return build(HttpStatus.BAD_REQUEST, message, request.getRequestURI());
    }

    /**
     * The framework message is safe to forward: it contains only the parameter name,
     * no internal implementation detail.
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiErrorResponse> handleMissingParam(
            MissingServletRequestParameterException ex, HttpServletRequest request) {
        logger.warn("Missing parameter on {}: {}", request.getRequestURI(), ex.getMessage());
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), request.getRequestURI());
    }

    /**
     * Returns a generic message instead of Jackson's internal error to avoid leaking
     * deserialization implementation details to the client.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleUnreadable(
            HttpMessageNotReadableException ex, HttpServletRequest request) {
        logger.warn("Malformed JSON on {}", request.getRequestURI());
        return build(HttpStatus.BAD_REQUEST, "Malformed JSON request", request.getRequestURI());
    }

    /**
     * Requires {@code spring.mvc.throw-exception-if-no-handler-found=true} and
     * {@code spring.web.resources.add-mappings=false} in {@code application.yml};
     * without those flags Spring swallows the 404 and returns its default error page.
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNoHandler(
            NoHandlerFoundException ex, HttpServletRequest request) {
        logger.warn("No handler found for {} {}", ex.getHttpMethod(), request.getRequestURI());
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), request.getRequestURI());
    }

    /**
     * Fallback for every unhandled exception. The full stack trace is logged here and
     * deliberately absent from the client response to avoid leaking internal detail.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGeneric(
            Exception ex, HttpServletRequest request) {
        logger.error("Unexpected error on {}", request.getRequestURI(), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred", request.getRequestURI());
    }

    private ResponseEntity<ApiErrorResponse> build(HttpStatus status, String message, String path) {
        return ResponseEntity.status(status).body(new ApiErrorResponse(
                status.value(),
                status.name(),
                message,
                Instant.now(),
                path
        ));
    }
}