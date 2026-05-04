package dev.webisbrian.vault.domain.model;

import java.time.Instant;
import java.util.UUID;

/**
 * Aggregate root representing a stored secret (API key, password, token, etc.).
 *
 * <p>Creation is exclusively through the {@link #create} factory method, which generates the
 * identity and sets both timestamps. Direct instantiation via constructor is intentionally
 * blocked to enforce valid-by-construction semantics.
 *
 * <p>Layer: domain/model — zero framework imports, pure Java only.
 */
public class Secret {

    private final UUID id;
    private SecretName name;
    private SecretValue value;
    private String description;
    private final Instant createdAt;
    private Instant updatedAt;

    private Secret(
            UUID id,
            SecretName name,
            SecretValue value,
            String description,
            Instant createdAt,
            Instant updatedAt) {
        this.id = id;
        this.name = name;
        this.value = value;
        this.description = description;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * Factory method for new domain objects.
     * Generates a random UUID and sets createdAt/updatedAt to the same instant.
     * Use this when creating a Secret for the first time in the application layer.
     */
    public static Secret create(SecretName name, SecretValue value, String description) {
        Instant now = Instant.now();
        return new Secret(UUID.randomUUID(), name, value, description, now, now);
    }

    /**
     * Factory method for rehydrating a Secret from persisted data.
     * All fields are supplied explicitly — no UUID generation, no timestamp generation.
     * Use this exclusively in persistence adapters when loading a row from the database.
     * Using {@link #create} there would overwrite the original identity and timestamps.
     */
    public static Secret reconstitute(
            UUID id,
            SecretName name,
            SecretValue value,
            String description,
            Instant createdAt,
            Instant updatedAt) {
        return new Secret(id, name, value, description, createdAt, updatedAt);
    }

    /**
     * Updates mutable fields and refreshes updatedAt.
     * id and createdAt are immutable after creation.
     */
    public void update(SecretName name, SecretValue value, String description) {
        this.name = name;
        this.value = value;
        this.description = description;
        this.updatedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public SecretName getName() { return name; }
    public SecretValue getValue() { return value; }
    public String getDescription() { return description; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}