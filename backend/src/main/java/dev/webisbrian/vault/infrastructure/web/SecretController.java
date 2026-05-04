package dev.webisbrian.vault.infrastructure.web;

import dev.webisbrian.vault.domain.model.Secret;
import dev.webisbrian.vault.domain.port.in.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST adapter for Secret CRUD operations. Delegates entirely to use-case ports —
 * no business logic lives here. Translation between DTOs and domain commands is
 * handled by {@link SecretWebMapper}.
 *
 * <p>The context path {@code /api/v1} is set globally in {@code application.yml};
 * this controller is mapped under {@code /secrets} relative to that root.
 *
 * <p>Layer: infrastructure/web — Spring MVC annotations are intentionally here.
 */
@RestController
@RequestMapping("/secrets")
@Tag(name = "Secrets", description = "CRUD operations for vault secrets")
public class SecretController {

    private final CreateSecretUseCase createSecret;
    private final GetSecretUseCase getSecret;
    private final ListSecretsUseCase listSecrets;
    private final UpdateSecretUseCase updateSecret;
    private final DeleteSecretUseCase deleteSecret;

    public SecretController(
            CreateSecretUseCase createSecret,
            GetSecretUseCase getSecret,
            ListSecretsUseCase listSecrets,
            UpdateSecretUseCase updateSecret,
            DeleteSecretUseCase deleteSecret) {
        this.createSecret = createSecret;
        this.getSecret = getSecret;
        this.listSecrets = listSecrets;
        this.updateSecret = updateSecret;
        this.deleteSecret = deleteSecret;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new secret")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Secret created"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "409", description = "Name already exists")
    })
    public SecretDetailResponse create(@Valid @RequestBody CreateSecretRequest request) {
        Secret secret = createSecret.execute(SecretWebMapper.toCreateCommand(request));
        return SecretWebMapper.toDetailResponse(secret);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a secret by ID — response includes the secret value")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Secret found"),
            @ApiResponse(responseCode = "404", description = "Secret not found")
    })
    public SecretDetailResponse getById(@PathVariable UUID id) {
        return SecretWebMapper.toDetailResponse(getSecret.execute(id));
    }

    @GetMapping
    @Operation(summary = "List secrets (paginated) — response omits secret values")
    @ApiResponse(responseCode = "200", description = "Paginated list of secrets")
    public PageResponse<SecretResponse> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return SecretWebMapper.toPageResponse(listSecrets.execute(page, size));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Replace all fields of an existing secret")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Secret updated"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "404", description = "Secret not found"),
            @ApiResponse(responseCode = "409", description = "Name already exists")
    })
    public SecretDetailResponse update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateSecretRequest request) {
        Secret secret = updateSecret.execute(id, SecretWebMapper.toUpdateCommand(request));
        return SecretWebMapper.toDetailResponse(secret);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a secret")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Secret deleted"),
            @ApiResponse(responseCode = "404", description = "Secret not found")
    })
    public void delete(@PathVariable UUID id) {
        deleteSecret.execute(id);
    }
}