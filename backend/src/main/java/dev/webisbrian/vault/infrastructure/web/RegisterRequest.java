package dev.webisbrian.vault.infrastructure.web;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for the POST /auth/register endpoint.
 *
 * <p>Bean Validation annotations enforce syntactic constraints at the web boundary.
 * Business invariants (value objects) re-validate in the domain layer as a second line of defence.
 *
 * <p>Layer: infrastructure/web — DTO, no domain imports.
 */
public record RegisterRequest(

        @NotBlank(message = "Email must not be blank")
        @Email(message = "Email format is invalid")
        String email,

        @NotBlank(message = "Password must not be blank")
        @Size(min = 12, max = 72, message = "Password must be between 12 and 72 characters")
        String password
) {}
