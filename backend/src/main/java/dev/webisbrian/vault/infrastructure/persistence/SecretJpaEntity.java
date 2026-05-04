package dev.webisbrian.vault.infrastructure.persistence;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

/**
 * JPA entity mapped to the {@code secrets} table. Carries only persistence concerns —
 * no business logic. Domain rules live in {@link dev.webisbrian.vault.domain.model.Secret}.
 *
 * <p>A protected no-arg constructor is required by the JPA specification; it must not be used
 * by application code. All instances are created via the package-private all-args constructor,
 * called exclusively from {@link SecretMapper}.
 *
 * <p>Layer: infrastructure/persistence — JPA annotations live here, NOT in the domain.
 */
@Entity
@Table(name = "secrets")
class SecretJpaEntity {

    @Id
    private UUID id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String value;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /** Required by JPA — do not use in application code. */
    protected SecretJpaEntity() {}

    SecretJpaEntity(UUID id, String name, String value, String description,
                    Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.name = name;
        this.value = value;
        this.description = description;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    UUID getId() { return id; }
    String getName() { return name; }
    String getValue() { return value; }
    String getDescription() { return description; }
    Instant getCreatedAt() { return createdAt; }
    Instant getUpdatedAt() { return updatedAt; }
}