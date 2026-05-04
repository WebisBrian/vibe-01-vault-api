package dev.webisbrian.vault.infrastructure.web;

import java.time.Instant;
import java.util.UUID;

/**
 * Response DTO for single-resource endpoints (GET /{id}, POST, PUT) — includes the
 * secret value. Must never be returned by list endpoints to avoid bulk value exposure.
 *
 * <p>Layer: infrastructure/web — DTO, no domain imports.
 */
public record SecretDetailResponse(
        UUID id,
        String name,
        String value,
        String description,
        Instant createdAt,
        Instant updatedAt
) {}