package dev.webisbrian.vault.domain.exception;

/**
 * Domain exception thrown when a Secret or one of its value objects violates a business invariant
 * (e.g. blank name, null value). Caught by the GlobalExceptionHandler in the infrastructure layer.
 *
 * <p>Layer: domain/exception — no framework imports, pure Java only.
 */
public class InvalidSecretException extends DomainException {

    public InvalidSecretException(String message) {
        super(message, "INVALID_SECRET");
    }
}