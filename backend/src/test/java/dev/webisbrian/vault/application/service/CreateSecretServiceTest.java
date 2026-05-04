package dev.webisbrian.vault.application.service;

import dev.webisbrian.vault.domain.exception.SecretAlreadyExistsException;
import dev.webisbrian.vault.domain.model.Secret;
import dev.webisbrian.vault.domain.model.SecretName;
import dev.webisbrian.vault.domain.model.SecretValue;
import dev.webisbrian.vault.domain.port.in.CreateSecretUseCase;
import dev.webisbrian.vault.domain.port.out.SecretRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateSecretServiceTest {

    @Mock
    private SecretRepository repository;

    private CreateSecretService service;

    @BeforeEach
    void setUp() {
        service = new CreateSecretService(repository);
    }

    @Test
    void should_create_secret_when_name_is_unique() {
        CreateSecretUseCase.Command command =
                new CreateSecretUseCase.Command("my-api-key", "s3cr3t", "a description");
        Secret saved = Secret.create(new SecretName("my-api-key"), new SecretValue("s3cr3t"), "a description");

        when(repository.existsByName(new SecretName("my-api-key"))).thenReturn(false);
        when(repository.save(any(Secret.class))).thenReturn(saved);

        Secret result = service.execute(command);

        assertThat(result).isSameAs(saved);
        verify(repository).existsByName(new SecretName("my-api-key"));
        verify(repository).save(any(Secret.class));
    }

    @Test
    void should_throw_when_creating_secret_with_duplicate_name() {
        CreateSecretUseCase.Command command =
                new CreateSecretUseCase.Command("my-api-key", "s3cr3t", null);

        when(repository.existsByName(new SecretName("my-api-key"))).thenReturn(true);

        assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(SecretAlreadyExistsException.class);
        verify(repository, never()).save(any());
    }
}