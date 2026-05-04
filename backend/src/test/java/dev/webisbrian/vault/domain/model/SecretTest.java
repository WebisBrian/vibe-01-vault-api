package dev.webisbrian.vault.domain.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SecretTest {

    private static final SecretName NAME = new SecretName("my-api-key");
    private static final SecretValue VALUE = new SecretValue("abc123");

    @Test
    void should_create_with_factory_method() {
        Secret secret = Secret.create(NAME, VALUE, "A test secret");

        assertThat(secret.getId()).isNotNull();
        assertThat(secret.getName()).isEqualTo(NAME);
        assertThat(secret.getValue()).isEqualTo(VALUE);
        assertThat(secret.getDescription()).isEqualTo("A test secret");
    }

    @Test
    void should_set_timestamps() {
        Instant before = Instant.now();
        Secret secret = Secret.create(NAME, VALUE, null);
        Instant after = Instant.now();

        assertThat(secret.getCreatedAt()).isBetween(before, after);
        assertThat(secret.getUpdatedAt()).isBetween(before, after);
        // Both timestamps come from the same Instant.now() call in create()
        assertThat(secret.getCreatedAt()).isEqualTo(secret.getUpdatedAt());
    }

    @Test
    void should_reconstitute_with_all_fields() {
        UUID id = UUID.randomUUID();
        Instant createdAt = Instant.parse("2026-01-01T10:00:00Z");
        Instant updatedAt = Instant.parse("2026-01-02T12:00:00Z");

        Secret secret = Secret.reconstitute(id, NAME, VALUE, "persisted description", createdAt, updatedAt);

        assertThat(secret.getId()).isEqualTo(id);
        assertThat(secret.getName()).isEqualTo(NAME);
        assertThat(secret.getValue()).isEqualTo(VALUE);
        assertThat(secret.getDescription()).isEqualTo("persisted description");
        assertThat(secret.getCreatedAt()).isEqualTo(createdAt);
        assertThat(secret.getUpdatedAt()).isEqualTo(updatedAt);
    }

    @Test
    void should_update_fields() {
        Secret secret = Secret.create(NAME, VALUE, "old description");

        SecretName newName = new SecretName("new-key");
        SecretValue newValue = new SecretValue("newval");

        Instant beforeUpdate = Instant.now();
        secret.update(newName, newValue, "new description");
        Instant afterUpdate = Instant.now();

        assertThat(secret.getName()).isEqualTo(newName);
        assertThat(secret.getValue()).isEqualTo(newValue);
        assertThat(secret.getDescription()).isEqualTo("new description");
        assertThat(secret.getUpdatedAt()).isBetween(beforeUpdate, afterUpdate);
    }
}