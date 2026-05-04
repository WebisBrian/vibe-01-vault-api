package dev.webisbrian.vault.application.service;

import dev.webisbrian.vault.domain.exception.UserAlreadyExistsException;
import dev.webisbrian.vault.domain.model.Email;
import dev.webisbrian.vault.domain.model.EncodedPassword;
import dev.webisbrian.vault.domain.model.RawPassword;
import dev.webisbrian.vault.domain.model.Role;
import dev.webisbrian.vault.domain.model.User;
import dev.webisbrian.vault.domain.port.in.RegisterUserUseCase;
import dev.webisbrian.vault.domain.port.out.PasswordEncoder;
import dev.webisbrian.vault.domain.port.out.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegisterUserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private RegisterUserService service;

    @BeforeEach
    void setUp() {
        service = new RegisterUserService(userRepository, passwordEncoder);
    }

    @Test
    void should_register_user_when_email_is_available() {
        RegisterUserUseCase.Command command =
                new RegisterUserUseCase.Command("user@example.com", "Password123!");
        EncodedPassword encodedPassword = EncodedPassword.of("$2a$10$hash");
        User savedUser = User.create(new Email("user@example.com"), encodedPassword, Role.MEMBER);

        when(userRepository.existsByEmail(new Email("user@example.com"))).thenReturn(false);
        when(passwordEncoder.encode(any(RawPassword.class))).thenReturn(encodedPassword);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        User result = service.execute(command);

        assertThat(result).isSameAs(savedUser);
        assertThat(result.getRole()).isEqualTo(Role.MEMBER);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void should_throw_when_email_already_exists() {
        RegisterUserUseCase.Command command =
                new RegisterUserUseCase.Command("user@example.com", "Password123!");

        when(userRepository.existsByEmail(new Email("user@example.com"))).thenReturn(true);

        assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(UserAlreadyExistsException.class);
        verify(userRepository, never()).save(any());
    }

    @Test
    void should_encode_password_before_saving() {
        RegisterUserUseCase.Command command =
                new RegisterUserUseCase.Command("user@example.com", "Password123!");
        EncodedPassword encodedPassword = EncodedPassword.of("$2a$10$hash");
        User savedUser = User.create(new Email("user@example.com"), encodedPassword, Role.MEMBER);

        when(userRepository.existsByEmail(any(Email.class))).thenReturn(false);
        when(passwordEncoder.encode(any(RawPassword.class))).thenReturn(encodedPassword);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        service.execute(command);

        verify(passwordEncoder).encode(argThat(p -> "Password123!".equals(p.value())));
    }

    @Test
    void should_always_assign_member_role() {
        RegisterUserUseCase.Command command =
                new RegisterUserUseCase.Command("user@example.com", "Password123!");
        EncodedPassword encodedPassword = EncodedPassword.of("$2a$10$hash");
        User savedUser = User.create(new Email("user@example.com"), encodedPassword, Role.MEMBER);

        when(userRepository.existsByEmail(any(Email.class))).thenReturn(false);
        when(passwordEncoder.encode(any(RawPassword.class))).thenReturn(encodedPassword);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        service.execute(command);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getRole()).isEqualTo(Role.MEMBER);
    }
}
