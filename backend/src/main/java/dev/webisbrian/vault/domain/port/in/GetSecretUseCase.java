package dev.webisbrian.vault.domain.port.in;

import dev.webisbrian.vault.domain.model.Secret;

import java.util.UUID;

/**
 * Inbound port — contract for retrieving a single Secret by its identifier.
 *
 * <p>Layer: domain/port/in — inbound port interface, zero framework imports.
 */
public interface GetSecretUseCase {

    /**
     * Retrieves a Secret by ID.
     *
     * @param id the Secret's unique identifier
     * @return the found Secret
     * @throws dev.webisbrian.vault.domain.exception.SecretNotFoundException if not found
     */
    Secret execute(UUID id);
}