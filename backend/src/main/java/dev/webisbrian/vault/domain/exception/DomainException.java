package dev.webisbrian.vault.domain.exception;

/**
 * Abstract base for all business exceptions in the domain layer.
 *
 * <p>Extends {@link RuntimeException} so exceptions propagate unchecked through the call stack;
 * callers are not forced to catch them. {@code GlobalExceptionHandler} is the single place where
 * they are caught and translated to HTTP responses.
 *
 * <p>{@code errorCode} carries a machine-readable identifier (e.g. {@code "SECRET_NOT_FOUND"})
 * that lets the HTTP layer choose the appropriate status code without the domain knowing
 * anything about HTTP.
 */
public abstract class DomainException extends RuntimeException {

    private final String errorCode;

    protected DomainException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}