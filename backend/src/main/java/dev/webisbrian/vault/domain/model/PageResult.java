package dev.webisbrian.vault.domain.model;

import java.util.List;

/**
 * Generic domain record for paginated query results.
 *
 * <p>Kept in the domain layer so use cases can return paginated data without depending on
 * Spring's {@code Page} type (which lives in the infrastructure). Adapters translate this
 * to whatever HTTP response shape is needed.
 *
 * <p>Layer: domain/model — zero framework imports, pure Java only.
 *
 * @param items         the items on the current page
 * @param page          zero-based page number
 * @param size          requested page size
 * @param totalElements total number of matching elements across all pages
 * @param totalPages    total number of pages
 */
public record PageResult<T>(
        List<T> items,
        int page,
        int size,
        long totalElements,
        int totalPages) {
}