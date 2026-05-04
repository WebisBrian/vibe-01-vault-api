package dev.webisbrian.vault.infrastructure.config;

import dev.webisbrian.vault.domain.model.Email;
import dev.webisbrian.vault.domain.model.EncodedPassword;
import dev.webisbrian.vault.domain.model.RawPassword;
import dev.webisbrian.vault.domain.model.Role;
import dev.webisbrian.vault.domain.model.User;
import dev.webisbrian.vault.domain.port.out.PasswordEncoder;
import dev.webisbrian.vault.domain.port.out.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Startup initializer that seeds an ADMIN user on first launch.
 *
 * <p>Runs once after the Spring context is ready. Checks {@link UserRepository#count()};
 * if the database is empty, it reads credentials from {@code vault.admin.email} and
 * {@code vault.admin.password} properties (mapped from {@code VAULT_ADMIN_EMAIL} /
 * {@code VAULT_ADMIN_PASSWORD} env vars) and persists an ADMIN user.
 *
 * <p>Idempotent — if any user already exists the initializer exits immediately without
 * making any changes. If the env vars are absent or blank it logs a warning and exits
 * without crashing the application.
 *
 * <p>Layer: infrastructure/config — depends on domain ports only, no JPA directly.
 */
@Component
public class AdminInitializer implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(AdminInitializer.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminEmail;
    private final String adminPassword;

    public AdminInitializer(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            @Value("${vault.admin.email:}") String adminEmail,
            @Value("${vault.admin.password:}") String adminPassword) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminEmail = adminEmail;
        this.adminPassword = adminPassword;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (userRepository.count() > 0) {
            logger.debug("Users already exist — skipping admin initialization.");
            return;
        }
        if (adminEmail.isBlank() || adminPassword.isBlank()) {
            logger.warn("VAULT_ADMIN_EMAIL or VAULT_ADMIN_PASSWORD is not set — admin user not created.");
            return;
        }
        Email email = new Email(adminEmail);
        RawPassword rawPassword = new RawPassword(adminPassword);
        EncodedPassword encodedPassword = passwordEncoder.encode(rawPassword);
        User admin = User.create(email, encodedPassword, Role.ADMIN);
        userRepository.save(admin);
        logger.info("Admin user created with email: {}", email.value());
    }
}
