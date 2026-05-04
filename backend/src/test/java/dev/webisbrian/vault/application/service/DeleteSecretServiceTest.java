package dev.webisbrian.vault.application.service;

import dev.webisbrian.vault.domain.exception.SecretNotFoundException;
import dev.webisbrian.vault.domain.model.Secret;
import dev.webisbrian.vault.domain.model.SecretName;
import dev.webisbrian.vault.domain.model.SecretValue;
import dev.webisbrian.vault.domain.port.out.SecretRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeleteSecretServiceTest {

    @Mock
    private SecretRepository repository;

    private DeleteSecretService service;

    @BeforeEach
    void setUp() {
        service = new DeleteSecretService(repository);
    }

    @Test
    void should_delete_secret_when_found() {
        UUID id = UUID.randomUUID();
        Secret secret = Secret.create(new SecretName("my-key"), new SecretValue("val"), null);

        when(repository.findById(id)).thenReturn(Optional.of(secret));

        service.execute(id);

        verify(repository).deleteById(id);
    }

    @Test
    void should_throw_when_deleting_nonexistent_secret() {
        UUID id = UUID.randomUUID();

        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute(id))
                .isInstanceOf(SecretNotFoundException.class);
        verify(repository, never()).deleteById(any());
    }
}