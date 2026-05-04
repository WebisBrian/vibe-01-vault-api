package dev.webisbrian.vault.domain.port.in;

import java.util.UUID;

/**
 * Inbound port — contract for deleting a Secret by its identifier.
 *
 * <p>Layer: domain/port/in — inbound port interface, zero framework imports.
 */
public interface DeleteSecretUseCase {

    /**
     * Deletes the Secret with the given ID.
     *
     * @param id the Secret's unique identifier
     * @throws dev.webisbrian.vault.domain.exception.SecretNotFoundException if not found
     */
    void execute(UUID id);
}