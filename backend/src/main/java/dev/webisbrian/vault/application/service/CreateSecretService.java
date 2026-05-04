package dev.webisbrian.vault.application.service;

import dev.webisbrian.vault.domain.exception.SecretAlreadyExistsException;
import dev.webisbrian.vault.domain.model.Secret;
import dev.webisbrian.vault.domain.model.SecretName;
import dev.webisbrian.vault.domain.model.SecretValue;
import dev.webisbrian.vault.domain.port.in.CreateSecretUseCase;
import dev.webisbrian.vault.domain.port.out.SecretRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Application service — implements the {@link CreateSecretUseCase} inbound port.
 *
 * <p>Orchestrates: name uniqueness check → domain object construction → persistence.
 * All business rules that require cross-entity or repository state live here;
 * pure invariants (blank name, null value) are enforced by the domain value objects.
 *
 * <p>Layer: application/service — depends on domain only, no framework imports.
 */
public class CreateSecretService implements CreateSecretUseCase {

    private static final Logger logger = LoggerFactory.getLogger(CreateSecretService.class);

    private final SecretRepository repository;

    public CreateSecretService(SecretRepository repository) {
        this.repository = repository;
    }

    /**
     * Creates and persists a new Secret.
     * Throws {@link SecretAlreadyExistsException} if the name is already taken.
     */
    @Override
    public Secret execute(Command command) {
        SecretName name = new SecretName(command.name());
        if (repository.existsByName(name)) {
            throw new SecretAlreadyExistsException(name);
        }
        Secret secret = Secret.create(name, new SecretValue(command.value()), command.description());
        Secret saved = repository.save(secret);
        logger.info("Secret created with id: {}", saved.getId());
        return saved;
    }
}