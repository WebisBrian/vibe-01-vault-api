package dev.webisbrian.vault.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/**
 * Spring Data JPA repository for {@link SecretJpaEntity}.
 *
 * <p>Provides standard CRUD operations via {@link JpaRepository} plus
 * a derived query for name-uniqueness checks. Application code must never
 * depend on this interface directly — all access goes through
 * {@link SecretPersistenceAdapter}, which implements the domain port.
 *
 * <p>Layer: infrastructure/persistence — Spring Data annotation, not visible to domain.
 */
interface SecretJpaRepository extends JpaRepository<SecretJpaEntity, UUID> {

    /** Returns {@code true} if a row with the given name already exists. */
    boolean existsByName(String name);
}