package dev.webisbrian.vault.infrastructure.web;

import java.util.List;

/**
 * Generic paginated response wrapper for list endpoints.
 * Mirrors the structure of the domain {@link dev.webisbrian.vault.domain.model.PageResult}
 * but lives in the web layer so the HTTP shape is decoupled from the domain record.
 *
 * <p>Layer: infrastructure/web — DTO, no domain logic.
 */
public record PageResponse<T>(
        List<T> items,
        int page,
        int size,
        long totalElements,
        int totalPages
) {}