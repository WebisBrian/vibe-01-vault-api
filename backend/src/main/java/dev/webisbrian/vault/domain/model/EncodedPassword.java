package dev.webisbrian.vault.domain.model;

import dev.webisbrian.vault.domain.exception.InvalidUserException;

import java.util.Objects;

/**
 * Value object representing a BCrypt-encoded password hash.
 *
 * <p>Opaque wrapper — no format validation beyond not blank (the hash format is determined by the
 * encoding implementation, not the domain). Created exclusively via the {@link #of} factory method.
 * Throws {@link InvalidUserException} if blank or null.
 *
 * <p>Never log or expose this value in responses.
 *
 * <p>Layer: domain/model — zero framework imports, pure Java only.
 */
public final class EncodedPassword {

    private final String value;

    private EncodedPassword(String value) {
        this.value = value;
    }

    /** Factory method — validates and wraps the encoded hash. */
    public static EncodedPassword of(String value) {
        if (value == null) {
            throw new InvalidUserException("Encoded password must not be null");
        }
        if (value.isBlank()) {
            throw new InvalidUserException("Encoded password must not be blank");
        }
        return new EncodedPassword(value);
    }

    /** Returns the encoded password hash. Never log or expose this in responses. */
    public String value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EncodedPassword that)) return false;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    /** Prevents accidental hash leak if this object is passed to a logger. */
    @Override
    public String toString() {
        return "EncodedPassword[PROTECTED]";
    }
}
