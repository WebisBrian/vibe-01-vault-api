package dev.webisbrian.vault.domain.exception;

import dev.webisbrian.vault.domain.model.SecretName;

/**
 * Domain exception thrown when attempting to create a Secret whose name is already taken.
 * Caught by the GlobalExceptionHandler in the infrastructure layer.
 *
 * <p>Layer: domain/exception — no framework imports, pure Java only.
 */
public class SecretAlreadyExistsException extends DomainException {

    public SecretAlreadyExistsException(SecretName name) {
        super("Secret already exists with name: " + name.value(), "SECRET_ALREADY_EXISTS");
    }
}