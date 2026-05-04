package dev.webisbrian.vault.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for {@link UserJpaEntity}.
 *
 * <p>Provides standard CRUD operations via {@link JpaRepository} (including {@code count()})
 * plus derived queries for email lookups. Application code must never depend on this interface
 * directly — all access goes through {@link UserPersistenceAdapter}, which implements the domain port.
 *
 * <p>Layer: infrastructure/persistence — Spring Data annotation, not visible to domain.
 */
interface UserJpaRepository extends JpaRepository<UserJpaEntity, UUID> {

    /** Finds a user row by email address. Returns empty if not found. */
    Optional<UserJpaEntity> findByEmail(String email);

    /** Returns {@code true} if a row with the given email already exists. */
    boolean existsByEmail(String email);
}
