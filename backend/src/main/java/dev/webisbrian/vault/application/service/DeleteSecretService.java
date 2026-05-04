package dev.webisbrian.vault.application.service;

import dev.webisbrian.vault.domain.exception.SecretNotFoundException;
import dev.webisbrian.vault.domain.port.in.DeleteSecretUseCase;
import dev.webisbrian.vault.domain.port.out.SecretRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

/**
 * Application service — implements the {@link DeleteSecretUseCase} inbound port.
 *
 * <p>Verifies the Secret exists before deleting — a delete on a missing ID is an explicit
 * error, not a silent no-op, so callers can distinguish "deleted" from "never existed".
 *
 * <p>Layer: application/service — depends on domain only, no framework imports.
 */
public class DeleteSecretService implements DeleteSecretUseCase {

    private static final Logger logger = LoggerFactory.getLogger(DeleteSecretService.class);

    private final SecretRepository repository;

    public DeleteSecretService(SecretRepository repository) {
        this.repository = repository;
    }

    @Override
    public void execute(UUID id) {
        repository.findById(id)
                .orElseThrow(() -> new SecretNotFoundException(id));
        repository.deleteById(id);
        logger.info("Secret deleted with id: {}", id);
    }
}