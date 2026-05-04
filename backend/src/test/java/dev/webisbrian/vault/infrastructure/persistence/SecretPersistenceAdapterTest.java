package dev.webisbrian.vault.infrastructure.persistence;

import dev.webisbrian.vault.domain.model.PageResult;
import dev.webisbrian.vault.domain.model.Secret;
import dev.webisbrian.vault.domain.model.SecretName;
import dev.webisbrian.vault.domain.model.SecretValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Slice test for {@link SecretPersistenceAdapter} against a real PostgreSQL instance
 * managed by Testcontainers via the TC JDBC URL in {@code application-test.yml}.
 *
 * <p>{@code @DataJpaTest} wraps each test in a rolled-back transaction, so tests start
 * with an empty {@code secrets} table without explicit cleanup.
 * {@code FlywayAutoConfiguration} is imported explicitly because {@code @DataJpaTest}
 * does not include Flyway in its slice by default.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@ImportAutoConfiguration(FlywayAutoConfiguration.class)
class SecretPersistenceAdapterTest {

    @Autowired
    private SecretJpaRepository jpaRepository;

    @Autowired
    private TestEntityManager em;

    private SecretPersistenceAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new SecretPersistenceAdapter(jpaRepository);
    }

    private Secret save(String name, String value, String description) {
        return adapter.save(Secret.create(new SecretName(name), new SecretValue(value), description));
    }

    @Test
    void should_save_and_find_by_id() {
        Secret saved = save("my-api-key", "s3cr3t", "a description");

        em.flush();
        em.clear();

        Optional<Secret> found = adapter.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(saved.getId());
        assertThat(found.get().getName().value()).isEqualTo("my-api-key");
        assertThat(found.get().getValue().value()).isEqualTo("s3cr3t");
        assertThat(found.get().getDescription()).isEqualTo("a description");
    }

    @Test
    void should_return_empty_when_not_found_by_id() {
        Optional<Secret> found = adapter.findById(UUID.randomUUID());

        assertThat(found).isEmpty();
    }

    @Test
    void should_find_all_paginated() {
        save("key-1", "val-1", null);
        save("key-2", "val-2", null);

        em.flush();
        em.clear();

        PageResult<Secret> result = adapter.findAll(0, 10);

        assertThat(result.items()).hasSize(2);
        assertThat(result.totalElements()).isEqualTo(2);
        assertThat(result.page()).isZero();
        assertThat(result.size()).isEqualTo(10);
    }

    @Test
    void should_return_true_when_name_exists() {
        save("existing-key", "val", null);
        em.flush();

        assertThat(adapter.existsByName(new SecretName("existing-key"))).isTrue();
    }

    @Test
    void should_return_false_when_name_does_not_exist() {
        assertThat(adapter.existsByName(new SecretName("nonexistent"))).isFalse();
    }

    @Test
    void should_delete_by_id() {
        Secret saved = save("to-delete", "val", null);
        em.flush();
        em.clear();

        adapter.deleteById(saved.getId());
        em.flush();

        assertThat(adapter.findById(saved.getId())).isEmpty();
    }
}