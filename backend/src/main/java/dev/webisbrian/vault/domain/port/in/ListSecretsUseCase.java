package dev.webisbrian.vault.domain.port.in;

import dev.webisbrian.vault.domain.model.PageResult;
import dev.webisbrian.vault.domain.model.Secret;

/**
 * Inbound port — contract for listing Secrets with pagination.
 *
 * <p>Layer: domain/port/in — inbound port interface, zero framework imports.
 */
public interface ListSecretsUseCase {

    /**
     * Returns a paginated list of Secrets.
     *
     * @param page zero-based page number
     * @param size number of items per page
     * @return paginated result containing Secrets and metadata
     */
    PageResult<Secret> execute(int page, int size);
}