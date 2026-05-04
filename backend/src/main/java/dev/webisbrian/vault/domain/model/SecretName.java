package dev.webisbrian.vault.domain.model;

import dev.webisbrian.vault.domain.exception.InvalidSecretException;

import java.util.Objects;

/**
 * Value object representing the name of a Secret.
 *
 * <p>Immutable by design — the same name cannot be mutated once created. Business invariants
 * (not null, not blank, max 255 chars) are enforced in the constructor, so an instance is
 * always valid. Throws {@link InvalidSecretException} rather than returning nulls or error codes.
 *
 * <p>Layer: domain/model — zero framework imports, pure Java only.
 */
public final class SecretName {

    private static final int MAX_LENGTH = 255;

    private final String value;

    public SecretName(String value) {
        if (value == null) {
            throw new InvalidSecretException("Secret name must not be null");
        }
        if (value.isBlank()) {
            throw new InvalidSecretException("Secret name must not be blank");
        }
        if (value.length() > MAX_LENGTH) {
            throw new InvalidSecretException(
                    "Secret name must not exceed " + MAX_LENGTH + " characters");
        }
        this.value = value;
    }

    /** Returns the raw string value. */
    public String value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SecretName that)) return false;
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