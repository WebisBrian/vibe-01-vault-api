package dev.webisbrian.vault.application.service;

import dev.webisbrian.vault.domain.model.PageResult;
import dev.webisbrian.vault.domain.model.Secret;
import dev.webisbrian.vault.domain.model.SecretName;
import dev.webisbrian.vault.domain.model.SecretValue;
import dev.webisbrian.vault.domain.port.out.SecretRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListSecretsServiceTest {

    @Mock
    private SecretRepository repository;

    private ListSecretsService service;

    @BeforeEach
    void setUp() {
        service = new ListSecretsService(repository);
    }

    @Test
    void should_return_page_when_listing_secrets() {
        List<Secret> items = List.of(
                Secret.create(new SecretName("key-1"), new SecretValue("val-1"), null),
                Secret.create(new SecretName("key-2"), new SecretValue("val-2"), null)
        );
        PageResult<Secret> page = new PageResult<>(items, 0, 10, 2, 1);

        when(repository.findAll(0, 10)).thenReturn(page);

        PageResult<Secret> result = service.execute(0, 10);

        assertThat(result).isSameAs(page);
        assertThat(result.items()).hasSize(2);
        assertThat(result.totalElements()).isEqualTo(2);
    }
}