package dev.webisbrian.vault.application.service;

import dev.webisbrian.vault.domain.exception.SecretNotFoundException;
import dev.webisbrian.vault.domain.model.Secret;
import dev.webisbrian.vault.domain.port.in.GetSecretUseCase;
import dev.webisbrian.vault.domain.port.out.SecretRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

/**
 * Application service — implements the {@link GetSecretUseCase} inbound port.
 *
 * <p>Retrieves a single Secret by its identifier. Throws {@link SecretNotFoundException}
 * if no Secret exists with the given ID, so the caller never receives an empty Optional.
 *
 * <p>Layer: application/service — depends on domain only, no framework imports.
 */
public class GetSecretService implements GetSecretUseCase {

    private static final Logger logger = LoggerFactory.getLogger(GetSecretService.class);

    private final SecretRepository repository;

    public GetSecretService(SecretRepository repository) {
        this.repository = repository;
    }

    @Override
    public Secret execute(UUID id) {
        Secret secret = repository.findById(id)
                .orElseThrow(() -> new SecretNotFoundException(id));
        logger.debug("Secret retrieved with id: {}", id);
        return secret;
    }
}