package dev.webisbrian.vault.domain.model;

import dev.webisbrian.vault.domain.exception.InvalidUserException;

/**
 * Value object representing a user's raw (plain-text) password before encoding.
 *
 * <p>Immutable and self-validating. Enforces not null, not blank, min 12 chars, max 72 chars
 * (BCrypt's effective input limit), and complexity — at least 3 of: uppercase, lowercase, digit,
 * special character. Throws {@link InvalidUserException} if any invariant is violated.
 *
 * <p>Never log or expose this value in responses.
 *
 * <p>Layer: domain/model — zero framework imports, pure Java only.
 */
public final class RawPassword {

    private static final int MIN_LENGTH = 12;
    /** BCrypt silently truncates input beyond 72 bytes; anything longer is effectively the same hash. */
    private static final int MAX_LENGTH = 72;

    private final String value;

    public RawPassword(String value) {
        if (value == null) {
            throw new InvalidUserException("Password must not be null");
        }
        if (value.isBlank()) {
            throw new InvalidUserException("Password must not be blank");
        }
        if (value.length() < MIN_LENGTH) {
            throw new InvalidUserException(
                    "Password must be at least " + MIN_LENGTH + " characters");
        }
        if (value.length() > MAX_LENGTH) {
            throw new InvalidUserException(
                    "Password must not exceed " + MAX_LENGTH + " characters");
        }
        if (countCategories(value) < 3) {
            throw new InvalidUserException(
                    "Password must contain at least 3 of: uppercase, lowercase, digit, special character");
        }
        this.value = value;
    }

    /** Returns the raw password string. Never log or expose this in responses. */
    public String value() {
        return value;
    }

    /** Prevents accidental password leak if this object is passed to a logger. */
    @Override
    public String toString() {
        return "RawPassword[PROTECTED]";
    }

    private static int countCategories(String value) {
        int count = 0;
        if (value.chars().anyMatch(Character::isUpperCase)) count++;
        if (value.chars().anyMatch(Character::isLowerCase)) count++;
        if (value.chars().anyMatch(Character::isDigit)) count++;
        if (value.chars().anyMatch(c -> !Character.isLetterOrDigit(c))) count++;
        return count;
    }
}
