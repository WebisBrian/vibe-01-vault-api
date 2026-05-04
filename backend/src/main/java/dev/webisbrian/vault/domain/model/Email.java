package dev.webisbrian.vault.domain.model;

import dev.webisbrian.vault.domain.exception.InvalidUserException;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Value object representing a user's email address.
 *
 * <p>Immutable by design. Business invariants are enforced in the constructor: not null, not blank,
 * max 255 chars, and must match a simple {@code local@domain} format (not full RFC 5322).
 * The value is normalized to lowercase on construction.
 * Throws {@link InvalidUserException} if any invariant is violated.
 *
 * <p>Layer: domain/model — zero framework imports, pure Java only.
 */
public final class Email {

    private static final int MAX_LENGTH = 255;
    // Requires text before @, the @ sign, then at least one dot in the domain part
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private final String value;

    public Email(String value) {
        if (value == null) {
            throw new InvalidUserException("Email must not be null");
        }
        if (value.isBlank()) {
            throw new InvalidUserException("Email must not be blank");
        }
        String normalized = value.toLowerCase();
        if (normalized.length() > MAX_LENGTH) {
            throw new InvalidUserException("Email must not exceed " + MAX_LENGTH + " characters");
        }
        if (!EMAIL_PATTERN.matcher(normalized).matches()) {
            throw new InvalidUserException("Email format is invalid");
        }
        this.value = normalized;
    }

    /** Returns the normalized (lowercase) email address. */
    public String value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Email that)) return false;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
