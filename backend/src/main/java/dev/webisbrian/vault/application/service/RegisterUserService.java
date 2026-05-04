package dev.webisbrian.vault.application.service;

import dev.webisbrian.vault.domain.exception.UserAlreadyExistsException;
import dev.webisbrian.vault.domain.model.Email;
import dev.webisbrian.vault.domain.model.EncodedPassword;
import dev.webisbrian.vault.domain.model.RawPassword;
import dev.webisbrian.vault.domain.model.Role;
import dev.webisbrian.vault.domain.model.User;
import dev.webisbrian.vault.domain.port.in.RegisterUserUseCase;
import dev.webisbrian.vault.domain.port.out.PasswordEncoder;
import dev.webisbrian.vault.domain.port.out.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Application service — implements the {@link RegisterUserUseCase} inbound port.
 *
 * <p>Orchestrates: email uniqueness check → password encoding → domain object construction →
 * persistence. All business rules that require cross-entity or repository state live here;
 * pure invariants (blank email, weak password) are enforced by the domain value objects.
 *
 * <p>Layer: application/service — depends on domain only, no framework imports.
 */
public class RegisterUserService implements RegisterUserUseCase {

    private static final Logger logger = LoggerFactory.getLogger(RegisterUserService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public RegisterUserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Registers a new User with the MEMBER role.
     * Throws {@link UserAlreadyExistsException} if the email is already taken.
     */
    @Override
    public User execute(Command command) {
        Email email = new Email(command.email());
        if (userRepository.existsByEmail(email)) {
            throw new UserAlreadyExistsException(email);
        }
        RawPassword rawPassword = new RawPassword(command.rawPassword());
        EncodedPassword encodedPassword = passwordEncoder.encode(rawPassword);
        User user = User.create(email, encodedPassword, Role.MEMBER);
        User saved = userRepository.save(user);
        logger.info("User registered with id: {}", saved.getId());
        return saved;
    }
}
