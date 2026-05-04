package dev.webisbrian.vault.infrastructure.web;

import dev.webisbrian.vault.domain.model.User;
import dev.webisbrian.vault.domain.port.in.RegisterUserUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST adapter for authentication endpoints. Delegates entirely to use-case ports —
 * no business logic lives here. Translation between DTOs and domain commands is
 * handled by {@link UserWebMapper}.
 *
 * <p>Login and token-refresh endpoints will be added to this controller in feature 2B.
 *
 * <p>The context path {@code /api/v1} is set globally in {@code application.yml};
 * this controller is mapped under {@code /auth} relative to that root.
 *
 * <p>Layer: infrastructure/web — Spring MVC annotations are intentionally here.
 */
@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "User registration and authentication")
public class AuthController {

    private final RegisterUserUseCase registerUser;

    public AuthController(RegisterUserUseCase registerUser) {
        this.registerUser = registerUser;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register a new user with the MEMBER role")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "User registered successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "409", description = "Email already taken")
    })
    public UserResponse register(@Valid @RequestBody RegisterRequest request) {
        User user = registerUser.execute(UserWebMapper.toCommand(request));
        return UserWebMapper.toResponse(user);
    }
}
