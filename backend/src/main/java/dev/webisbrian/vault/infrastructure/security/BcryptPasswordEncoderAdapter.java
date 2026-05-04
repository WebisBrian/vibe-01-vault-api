package dev.webisbrian.vault.infrastructure.security;

import dev.webisbrian.vault.domain.model.EncodedPassword;
import dev.webisbrian.vault.domain.model.RawPassword;
import dev.webisbrian.vault.domain.port.out.PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Outbound adapter — implements the domain {@link PasswordEncoder} port using BCrypt.
 *
 * <p>Wraps Spring Security's {@link BCryptPasswordEncoder} so the domain layer has no
 * dependency on Spring Security. The domain port defines the contract; this adapter
 * fulfills it using a concrete encoding algorithm.
 *
 * <p>Layer: infrastructure/security — Spring annotation intentionally here, not in the domain.
 */
@Component
public class BcryptPasswordEncoderAdapter implements PasswordEncoder {

    private final BCryptPasswordEncoder bcrypt = new BCryptPasswordEncoder();

    /**
     * Encodes a raw password using BCrypt and wraps the result in an {@link EncodedPassword}.
     */
    @Override
    public EncodedPassword encode(RawPassword rawPassword) {
        return EncodedPassword.of(bcrypt.encode(rawPassword.value()));
    }
}
