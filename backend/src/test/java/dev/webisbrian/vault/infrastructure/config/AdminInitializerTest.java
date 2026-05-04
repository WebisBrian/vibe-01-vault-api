package dev.webisbrian.vault.infrastructure.config;

import dev.webisbrian.vault.domain.model.Email;
import dev.webisbrian.vault.domain.model.EncodedPassword;
import dev.webisbrian.vault.domain.model.RawPassword;
import dev.webisbrian.vault.domain.model.Role;
import dev.webisbrian.vault.domain.model.User;
import dev.webisbrian.vault.domain.port.out.PasswordEncoder;
import dev.webisbrian.vault.domain.port.out.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminInitializerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Test
    void should_create_admin_when_no_users_exist() throws Exception {
        AdminInitializer initializer = new AdminInitializer(
                userRepository, passwordEncoder,
                "admin@example.com", "AdminPassword123!");
        EncodedPassword encoded = EncodedPassword.of("$2a$10$hash");

        when(userRepository.count()).thenReturn(0L);
        when(passwordEncoder.encode(any(RawPassword.class))).thenReturn(encoded);
        when(userRepository.save(any(User.class))).thenReturn(
                User.create(new Email("admin@example.com"), encoded, Role.ADMIN));

        initializer.run(null);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getRole()).isEqualTo(Role.ADMIN);
        assertThat(captor.getValue().getEmail().value()).isEqualTo("admin@example.com");
    }

    @Test
    void should_not_create_admin_when_users_exist() throws Exception {
        AdminInitializer initializer = new AdminInitializer(
                userRepository, passwordEncoder,
                "admin@example.com", "AdminPassword123!");

        when(userRepository.count()).thenReturn(1L);

        initializer.run(null);

        verify(userRepository, never()).save(any());
    }

    @Test
    void should_not_crash_when_env_vars_missing() throws Exception {
        AdminInitializer initializer = new AdminInitializer(
                userRepository, passwordEncoder,
                "", "");

        when(userRepository.count()).thenReturn(0L);

        assertThatCode(() -> initializer.run(null)).doesNotThrowAnyException();
        verify(userRepository, never()).save(any());
    }
}
