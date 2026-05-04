package dev.webisbrian.vault.application.service;

import dev.webisbrian.vault.domain.model.PageResult;
import dev.webisbrian.vault.domain.model.Secret;
import dev.webisbrian.vault.domain.port.in.ListSecretsUseCase;
import dev.webisbrian.vault.domain.port.out.SecretRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Application service — implements the {@link ListSecretsUseCase} inbound port.
 *
 * <p>Delegates directly to the repository for pagination. The domain {@link PageResult}
 * type shields the application layer from Spring's {@code Page} abstraction.
 *
 * <p>Layer: application/service — depends on domain only, no framework imports.
 */
public class ListSecretsService implements ListSecretsUseCase {

    private static final Logger logger = LoggerFactory.getLogger(ListSecretsService.class);

    private final SecretRepository repository;

    public ListSecretsService(SecretRepository repository) {
        this.repository = repository;
    }

    @Override
    public PageResult<Secret> execute(int page, int size) {
        logger.debug("Listing secrets — page: {}, size: {}", page, size);
        return repository.findAll(page, size);
    }
}