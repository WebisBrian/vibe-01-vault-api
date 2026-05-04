package dev.webisbrian.vault.infrastructure.config;

import dev.webisbrian.vault.application.service.*;
import dev.webisbrian.vault.domain.port.in.*;
import dev.webisbrian.vault.domain.port.out.PasswordEncoder;
import dev.webisbrian.vault.domain.port.out.SecretRepository;
import dev.webisbrian.vault.domain.port.out.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Infrastructure configuration that wires application-layer services as Spring beans.
 *
 * <p>Application services are plain Java classes with no Spring annotations — they depend
 * only on the domain. This {@code @Configuration} class acts as the composition root for
 * the application layer, instantiating each use-case service and injecting the domain
 * ports ({@link SecretRepository}, {@link UserRepository}, {@link PasswordEncoder})
 * fulfilled by their respective infrastructure adapters.
 *
 * <p>Layer: infrastructure/config — the only place where application services meet the
 * Spring container.
 */
@Configuration
public class ApplicationConfig {

    @Bean
    public RegisterUserUseCase registerUserUseCase(UserRepository userRepository,
                                                   PasswordEncoder passwordEncoder) {
        return new RegisterUserService(userRepository, passwordEncoder);
    }

    @Bean
    public CreateSecretUseCase createSecretUseCase(SecretRepository secretRepository) {
        return new CreateSecretService(secretRepository);
    }

    @Bean
    public GetSecretUseCase getSecretUseCase(SecretRepository secretRepository) {
        return new GetSecretService(secretRepository);
    }

    @Bean
    public ListSecretsUseCase listSecretsUseCase(SecretRepository secretRepository) {
        return new ListSecretsService(secretRepository);
    }

    @Bean
    public UpdateSecretUseCase updateSecretUseCase(SecretRepository secretRepository) {
        return new UpdateSecretService(secretRepository);
    }

    @Bean
    public DeleteSecretUseCase deleteSecretUseCase(SecretRepository secretRepository) {
        return new DeleteSecretService(secretRepository);
    }
}