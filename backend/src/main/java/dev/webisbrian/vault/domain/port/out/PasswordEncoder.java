package dev.webisbrian.vault.domain.port.out;

import dev.webisbrian.vault.domain.model.EncodedPassword;
import dev.webisbrian.vault.domain.model.RawPassword;

/**
 * Outbound port — contract for encoding raw passwords into secure hashes.
 *
 * <p>The infrastructure layer provides the concrete adapter (BCrypt, etc.) that implements this
 * interface. This port ensures the domain has no dependency on any specific encoding library.
 *
 * <p>Layer: domain/port/out — outbound port interface, zero framework imports.
 */
public interface PasswordEncoder {

    /**
     * Encodes a raw password into a secure hash.
     *
     * @param rawPassword the plain-text password to encode
     * @return the encoded password hash
     */
    EncodedPassword encode(RawPassword rawPassword);
}
