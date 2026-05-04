package dev.webisbrian.vault.application.service;

import dev.webisbrian.vault.domain.exception.SecretNotFoundException;
import dev.webisbrian.vault.domain.model.Secret;
import dev.webisbrian.vault.domain.model.SecretName;
import dev.webisbrian.vault.domain.model.SecretValue;
import dev.webisbrian.vault.domain.port.in.UpdateSecretUseCase;
import dev.webisbrian.vault.domain.port.out.SecretRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateSecretServiceTest {

    @Mock
    private SecretRepository repository;

    private UpdateSecretService service;

    @BeforeEach
    void setUp() {
        service = new UpdateSecretService(repository);
    }

    @Test
    void should_update_secret_when_found() {
        UUID id = UUID.randomUUID();
        Secret existing = Secret.create(new SecretName("old-name"), new SecretValue("old-val"), "old desc");
        UpdateSecretUseCase.Command command =
                new UpdateSecretUseCase.Command("new-name", "new-val", "new desc");

        when(repository.findById(id)).thenReturn(Optional.of(existing));
        when(repository.existsByName(new SecretName("new-name"))).thenReturn(false);
        when(repository.save(any(Secret.class))).thenReturn(existing);

        Secret result = service.execute(id, command);

        assertThat(result.getName().value()).isEqualTo("new-name");
        assertThat(result.getValue().value()).isEqualTo("new-val");
        assertThat(result.getDescription()).isEqualTo("new desc");
        verify(repository).save(existing);
    }

    @Test
    void should_throw_when_updating_nonexistent_secret() {
        UUID id = UUID.randomUUID();
        UpdateSecretUseCase.Command command =
                new UpdateSecretUseCase.Command("new-name", "new-val", null);

        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute(id, command))
                .isInstanceOf(SecretNotFoundException.class);
        verify(repository, never()).save(any());
    }
}