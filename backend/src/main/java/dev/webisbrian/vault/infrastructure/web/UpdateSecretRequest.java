package dev.webisbrian.vault.infrastructure.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for the PUT /secrets/{id} endpoint.
 * Carries the replacement values for all mutable fields of a Secret.
 *
 * <p>Layer: infrastructure/web — DTO, no domain imports.
 */
public record UpdateSecretRequest(

        @NotBlank(message = "Name must not be blank")
        @Size(max = 255, message = "Name must not exceed 255 characters")
        String name,

        @NotBlank(message = "Value must not be blank")
        String value,

        String description
) {}