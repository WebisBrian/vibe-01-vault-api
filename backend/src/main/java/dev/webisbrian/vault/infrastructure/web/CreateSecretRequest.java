package dev.webisbrian.vault.infrastructure.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for the POST /secrets endpoint.
 *
 * <p>Bean Validation annotations enforce syntactic constraints at the web boundary.
 * Business invariants (value objects) re-validate in the domain layer as a second line of defence.
 *
 * <p>Layer: infrastructure/web — DTO, no domain imports.
 */
public record CreateSecretRequest(

        @NotBlank(message = "Name must not be blank")
        @Size(max = 255, message = "Name must not exceed 255 characters")
        String name,

        @NotBlank(message = "Value must not be blank")
        String value,

        String description
) {}