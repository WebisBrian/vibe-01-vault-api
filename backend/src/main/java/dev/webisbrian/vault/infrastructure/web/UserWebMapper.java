package dev.webisbrian.vault.infrastructure.web;

import dev.webisbrian.vault.domain.model.User;
import dev.webisbrian.vault.domain.port.in.RegisterUserUseCase;

/**
 * Stateless mapper between user web DTOs and domain commands / domain objects.
 *
 * <p>Keeps conversion logic out of the controller so that the controller only
 * concerns itself with HTTP routing and status codes.
 *
 * <p>Layer: infrastructure/web — knows both web DTOs and domain types; intentionally here.
 */
class UserWebMapper {

    private UserWebMapper() {}

    /** Converts a registration request to the use-case command. */
    static RegisterUserUseCase.Command toCommand(RegisterRequest request) {
        return new RegisterUserUseCase.Command(request.email(), request.password());
    }

    /** Converts a domain User to the response DTO, omitting the password. */
    static UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail().value(),
                user.getRole().name(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
