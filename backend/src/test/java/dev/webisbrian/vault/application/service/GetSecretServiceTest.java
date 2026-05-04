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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetSecretServiceTest {

    @Mock
    private SecretRepository repository;

    private GetSecretService service;

    @BeforeEach
    void setUp() {
        service = new GetSecretService(repository);
    }

    @Test
    void should_return_secret_when_found_by_id() {
        UUID id = UUID.randomUUID();
        Secret secret = Secret.create(new SecretName("my-key"), new SecretValue("abc123"), null);

        when(repository.findById(id)).thenReturn(Optional.of(secret));

        Secret result = service.execute(id);

        assertThat(result).isSameAs(secret);
    }

    @Test
    void should_throw_when_secret_not_found_by_id() {
        UUID id = UUID.randomUUID();

        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute(id))
                .isInstanceOf(SecretNotFoundException.class);
    }
}