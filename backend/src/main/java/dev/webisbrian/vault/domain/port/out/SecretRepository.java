package dev.webisbrian.vault.domain.port.out;

import dev.webisbrian.vault.domain.model.PageResult;
import dev.webisbrian.vault.domain.model.Secret;
import dev.webisbrian.vault.domain.model.SecretName;

import java.util.Optional;
import java.util.UUID;

/**
 * Outbound port — defines the persistence contract for Secrets from the domain's perspective.
 *
 * <p>The infrastructure layer provides the concrete adapter (JPA, in-memory, etc.) that
 * implements this interface. The domain and application layers depend only on this interface,
 * never on the adapter directly (Dependency Inversion Principle).
 *
 * <p>Layer: domain/port/out — outbound port interface, zero framework imports.
 */
public interface SecretRepository {

    /** Persists a Secret (insert or update) and returns the saved instance. */
    Secret save(Secret secret);

    /** Finds a Secret by its unique identifier. Returns empty if not found. */
    Optional<Secret> findById(UUID id);

    /** Returns a paginated slice of all Secrets. */
    PageResult<Secret> findAll(int page, int size);

    /** Returns {@code true} if a Secret with the given name already exists. */
    boolean existsByName(SecretName name);

    /** Deletes the Secret with the given identifier. No-op if not found. */
    void deleteById(UUID id);
}