package dev.webisbrian.vault.domain.exception;

import dev.webisbrian.vault.domain.model.Email;

/**
 * Domain exception thrown when attempting to register a User whose email is already taken.
 * Caught by GlobalExceptionHandler → HTTP 409.
 *
 * <p>Layer: domain/exception — no framework imports, pure Java only.
 */
public class UserAlreadyExistsException extends DomainException {

    public UserAlreadyExistsException(Email email) {
        super("User already exists with email: " + email.value(), "USER_ALREADY_EXISTS");
    }
}
