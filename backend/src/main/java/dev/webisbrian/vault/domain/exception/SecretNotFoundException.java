package dev.webisbrian.vault.domain.exception;

import java.util.UUID;

/**
 * Domain exception thrown when a Secret cannot be found by its identifier.
 * Caught by the GlobalExceptionHandler in the infrastructure layer.
 *
 * <p>Layer: domain/exception — no framework imports, pure Java only.
 */
public class SecretNotFoundException extends DomainException {

    public SecretNotFoundException(UUID id) {
        super("Secret not found with id: " + id, "SECRET_NOT_FOUND");
    }
}