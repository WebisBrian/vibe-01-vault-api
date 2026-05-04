package dev.webisbrian.vault.domain.model;

import dev.webisbrian.vault.domain.exception.InvalidSecretException;

import java.util.Objects;

/**
 * Value object representing the sensitive payload of a Secret (e.g. an API key or password).
 *
 * <p>Immutable by design — enforces not-null and not-blank invariants in the constructor.
 * The infrastructure layer is responsible for encrypting the raw value before persistence;
 * the domain holds only the plaintext representation in memory.
 *
 * <p>Layer: domain/model — zero framework imports, pure Java only.
 */
public final class SecretValue {

    private final String value;

    public SecretValue(String value) {
        if (value == null) {
            throw new InvalidSecretException("Secret value must not be null");
        }
        if (value.isBlank()) {
            throw new InvalidSecretException("Secret value must not be blank");
        }
        this.value = value;
    }

    /** Returns the raw plaintext value. Never log or expose this in responses. */
    public String value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SecretValue that)) return false;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}