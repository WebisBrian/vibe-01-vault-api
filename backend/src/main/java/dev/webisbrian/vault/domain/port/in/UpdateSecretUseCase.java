package dev.webisbrian.vault.domain.port.in;

import dev.webisbrian.vault.domain.model.Secret;

import java.util.UUID;

/**
 * Inbound port — contract for updating an existing Secret.
 *
 * <p>Layer: domain/port/in — inbound port interface, zero framework imports.
 */
public interface UpdateSecretUseCase {

    /** Carries the new values for an update operation. */
    record Command(String name, String value, String description) {}

    /**
     * Updates name, value, and description of an existing Secret.
     *
     * @param id      the Secret's unique identifier
     * @param command the replacement values
     * @return the updated Secret
     * @throws dev.webisbrian.vault.domain.exception.SecretNotFoundException     if not found
     * @throws dev.webisbrian.vault.domain.exception.SecretAlreadyExistsException if the new name conflicts
     */
    Secret execute(UUID id, Command command);
}