package dev.webisbrian.vault.domain.port.in;

import dev.webisbrian.vault.domain.model.Secret;

/**
 * Inbound port — contract for creating a new Secret.
 *
 * <p>The {@link Command} record is defined here as an inner type so the interface and its
 * input are co-located (one class to import, one place to change the contract).
 *
 * <p>Layer: domain/port/in — inbound port interface, zero framework imports.
 */
public interface CreateSecretUseCase {

    /** Carries the raw inputs for secret creation before domain objects are constructed. */
    record Command(String name, String value, String description) {}

    /**
     * Creates and persists a new Secret.
     *
     * @param command the creation inputs
     * @return the persisted Secret
     */
    Secret execute(Command command);
}