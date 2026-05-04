package dev.webisbrian.vault.domain.model;

import java.time.Instant;
import java.util.UUID;

/**
 * Aggregate root representing a registered user of the vault.
 *
 * <p>Creation is exclusively through the {@link #create} factory method, which generates the
 * identity and sets both timestamps. Rehydration from persistence uses {@link #reconstitute}.
 * Direct instantiation via constructor is intentionally blocked to enforce valid-by-construction
 * semantics.
 *
 * <p>Layer: domain/model — zero framework imports, pure Java only.
 */
public final class User {

    private final UUID id;
    private final Email email;
    private final EncodedPassword password;
    private final Role role;
    private final Instant createdAt;
    private final Instant updatedAt;

    private User(
            UUID id,
            Email email,
            EncodedPassword password,
            Role role,
            Instant createdAt,
            Instant updatedAt) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.role = role;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * Factory method for new users.
     * Generates a random UUID and sets createdAt/updatedAt to the same instant.
     * Use this when registering a User for the first time in the application layer.
     */
    public static User create(Email email, EncodedPassword password, Role role) {
        Instant now = Instant.now();
        return new User(UUID.randomUUID(), email, password, role, now, now);
    }

    /**
     * Factory method for rehydrating a User from persisted data.
     * All fields are supplied explicitly — no UUID generation, no timestamp generation.
     * Use this exclusively in persistence adapters when loading a row from the database.
     * Using {@link #create} there would overwrite the original identity and timestamps.
     */
    public static User reconstitute(
            UUID id,
            Email email,
            EncodedPassword password,
            Role role,
            Instant createdAt,
            Instant updatedAt) {
        return new User(id, email, password, role, createdAt, updatedAt);
    }

    public UUID getId() { return id; }
    public Email getEmail() { return email; }
    public EncodedPassword getPassword() { return password; }
    public Role getRole() { return role; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
