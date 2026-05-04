package dev.webisbrian.vault.domain.port.out;

import dev.webisbrian.vault.domain.model.Email;
import dev.webisbrian.vault.domain.model.User;

import java.util.Optional;

/**
 * Outbound port — defines the persistence contract for Users from the domain's perspective.
 *
 * <p>The infrastructure layer provides the concrete adapter (JPA, etc.) that implements this
 * interface. The domain and application layers depend only on this interface, never on the
 * adapter directly (Dependency Inversion Principle).
 *
 * <p>Layer: domain/port/out — outbound port interface, zero framework imports.
 */
public interface UserRepository {

    /** Persists a User (insert or update) and returns the saved instance. */
    User save(User user);

    /** Finds a User by email address. Returns empty if not found. */
    Optional<User> findByEmail(Email email);

    /** Returns {@code true} if a User with the given email already exists. */
    boolean existsByEmail(Email email);

    /** Returns the total number of registered users. */
    long count();
}
