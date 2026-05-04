package dev.webisbrian.vault.infrastructure.persistence;

import dev.webisbrian.vault.domain.model.Role;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * JPA entity mapped to the {@code users} table. Carries only persistence concerns —
 * no business logic. Domain rules live in {@link dev.webisbrian.vault.domain.model.User}.
 *
 * <p>A protected no-arg constructor is required by the JPA specification; it must not be used
 * by application code. All instances are created via the package-private all-args constructor,
 * called exclusively from {@link UserMapper}.
 *
 * <p>Layer: infrastructure/persistence — JPA annotations live here, NOT in the domain.
 */
@Entity
@Table(name = "users")
class UserJpaEntity {

    @Id
    private UUID id;

    @Column(nullable = false, length = 255, unique = true)
    private String email;

    @Column(nullable = false, length = 255)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private Role role;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /** Required by JPA — do not use in application code. */
    protected UserJpaEntity() {}

    UserJpaEntity(UUID id, String email, String password, Role role,
                  Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.role = role;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    UUID getId() { return id; }
    String getEmail() { return email; }
    String getPassword() { return password; }
    Role getRole() { return role; }
    Instant getCreatedAt() { return createdAt; }
    Instant getUpdatedAt() { return updatedAt; }
}
