package dev.webisbrian.vault.application.service;

import dev.webisbrian.vault.domain.exception.SecretAlreadyExistsException;
import dev.webisbrian.vault.domain.exception.SecretNotFoundException;
import dev.webisbrian.vault.domain.model.Secret;
import dev.webisbrian.vault.domain.model.SecretName;
import dev.webisbrian.vault.domain.model.SecretValue;
import dev.webisbrian.vault.domain.port.in.UpdateSecretUseCase;
import dev.webisbrian.vault.domain.port.out.SecretRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

/**
 * Application service — implements the {@link UpdateSecretUseCase} inbound port.
 *
 * <p>Orchestrates: existence check → conditional name uniqueness check → domain mutation → persistence.
 * The uniqueness check is skipped when the name is unchanged, so updating other fields on a secret
 * never triggers a false conflict.
 *
 * <p>Layer: application/service — depends on domain only, no framework imports.
 */
public class UpdateSecretService implements UpdateSecretUseCase {

    private static final Logger logger = LoggerFactory.getLogger(UpdateSecretService.class);

    private final SecretRepository repository;

    public UpdateSecretService(SecretRepository repository) {
        this.repository = repository;
    }

    @Override
    public Secret execute(UUID id, Command command) {
        Secret secret = repository.findById(id)
                .orElseThrow(() -> new SecretNotFoundException(id));

        SecretName newName = new SecretName(command.name());
        // Only check uniqueness when the name is actually changing
        if (!newName.equals(secret.getName()) && repository.existsByName(newName)) {
            throw new SecretAlreadyExistsException(newName);
        }

        secret.update(newName, new SecretValue(command.value()), command.description());
        Secret saved = repository.save(secret);
        logger.info("Secret updated with id: {}", id);
        return saved;
    }
}