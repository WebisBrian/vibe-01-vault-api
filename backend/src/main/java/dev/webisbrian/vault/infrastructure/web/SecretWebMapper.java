package dev.webisbrian.vault.infrastructure.web;

import dev.webisbrian.vault.domain.model.PageResult;
import dev.webisbrian.vault.domain.model.Secret;
import dev.webisbrian.vault.domain.port.in.CreateSecretUseCase;
import dev.webisbrian.vault.domain.port.in.UpdateSecretUseCase;

import java.util.List;

/**
 * Stateless mapper between web DTOs and domain commands / domain objects.
 *
 * <p>Keeps conversion logic out of the controller so that the controller only
 * concerns itself with HTTP routing and status codes.
 *
 * <p>Layer: infrastructure/web — knows both web DTOs and domain types; intentionally here.
 */
class SecretWebMapper {

    private SecretWebMapper() {}

    static CreateSecretUseCase.Command toCreateCommand(CreateSecretRequest request) {
        return new CreateSecretUseCase.Command(request.name(), request.value(), request.description());
    }

    static UpdateSecretUseCase.Command toUpdateCommand(UpdateSecretRequest request) {
        return new UpdateSecretUseCase.Command(request.name(), request.value(), request.description());
    }

    /** Maps to the detail response that includes the secret value. */
    static SecretDetailResponse toDetailResponse(Secret secret) {
        return new SecretDetailResponse(
                secret.getId(),
                secret.getName().value(),
                secret.getValue().value(),
                secret.getDescription(),
                secret.getCreatedAt(),
                secret.getUpdatedAt()
        );
    }

    /** Maps to the summary response that omits the secret value (safe for list endpoints). */
    static SecretResponse toResponse(Secret secret) {
        return new SecretResponse(
                secret.getId(),
                secret.getName().value(),
                secret.getDescription(),
                secret.getCreatedAt(),
                secret.getUpdatedAt()
        );
    }

    static PageResponse<SecretResponse> toPageResponse(PageResult<Secret> result) {
        List<SecretResponse> items = result.items().stream()
                .map(SecretWebMapper::toResponse)
                .toList();
        return new PageResponse<>(items, result.page(), result.size(),
                result.totalElements(), result.totalPages());
    }
}