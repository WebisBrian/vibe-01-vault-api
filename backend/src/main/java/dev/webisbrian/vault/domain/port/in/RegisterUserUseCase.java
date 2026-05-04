package dev.webisbrian.vault.domain.port.in;

import dev.webisbrian.vault.domain.model.User;

/**
 * Inbound port — contract for registering a new User.
 *
 * <p>The {@link Command} record is defined here as an inner type so the interface and its
 * input are co-located (one class to import, one place to change the contract).
 * Role is not part of the command — new users are always registered as {@code MEMBER}.
 *
 * <p>Layer: domain/port/in — inbound port interface, zero framework imports.
 */
public interface RegisterUserUseCase {

    /** Carries the raw inputs for user registration before domain objects are constructed. */
    record Command(String email, String rawPassword) {}

    /**
     * Registers a new User with the MEMBER role.
     *
     * @param command the registration inputs
     * @return the persisted User
     */
    User execute(Command command);
}
