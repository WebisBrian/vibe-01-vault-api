package dev.webisbrian.vault.infrastructure.web;

import java.time.Instant;
import java.util.UUID;

/**
 * Response DTO for user endpoints. Deliberately omits the password field.
 *
 * <p>Layer: infrastructure/web — DTO, no domain imports.
 */
public record UserResponse(
        UUID id,
        String email,
        String role,
        Instant createdAt,
        Instant updatedAt
) {}
