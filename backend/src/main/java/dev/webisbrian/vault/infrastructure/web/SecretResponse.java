package dev.webisbrian.vault.infrastructure.web;

import java.time.Instant;
import java.util.UUID;

/**
 * Response DTO for list endpoints — intentionally omits the secret value so that
 * bulk listing does not expose sensitive payloads. Use {@link SecretDetailResponse}
 * for single-resource endpoints that require the value.
 *
 * <p>Layer: infrastructure/web — DTO, no domain imports.
 */
public record SecretResponse(
        UUID id,
        String name,
        String description,
        Instant createdAt,
        Instant updatedAt
) {}