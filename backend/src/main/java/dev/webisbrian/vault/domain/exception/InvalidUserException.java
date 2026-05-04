package dev.webisbrian.vault.domain.exception;

/**
 * Domain exception thrown when a User or one of its value objects violates a business invariant
 * (e.g. blank email, password too short). Caught by GlobalExceptionHandler → HTTP 400.
 *
 * <p>Layer: domain/exception — no framework imports, pure Java only.
 */
public class InvalidUserException extends DomainException {

    public InvalidUserException(String message) {
        super(message, "INVALID_USER");
    }
}
