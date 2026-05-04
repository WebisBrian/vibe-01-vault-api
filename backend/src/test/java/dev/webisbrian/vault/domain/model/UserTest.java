package dev.webisbrian.vault.domain.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UserTest {

    private static final Email EMAIL = new Email("user@example.com");
    private static final EncodedPassword PASSWORD = EncodedPassword.of("$2a$10$hash");
    private static final Role ROLE = Role.MEMBER;

    @Test
    void should_create_with_factory_method() {
        User user = User.create(EMAIL, PASSWORD, ROLE);

        assertThat(user.getId()).isNotNull();
        assertThat(user.getEmail()).isEqualTo(EMAIL);
        assertThat(user.getPassword()).isEqualTo(PASSWORD);
        assertThat(user.getRole()).isEqualTo(ROLE);
    }

    @Test
    void should_set_timestamps_on_create() {
        Instant before = Instant.now();
        User user = User.create(EMAIL, PASSWORD, ROLE);
        Instant after = Instant.now();

        assertThat(user.getCreatedAt()).isBetween(before, after);
        assertThat(user.getUpdatedAt()).isBetween(before, after);
        // Both timestamps come from the same Instant.now() call in create()
        assertThat(user.getCreatedAt()).isEqualTo(user.getUpdatedAt());
    }

    @Test
    void should_reconstitute_with_all_fields() {
        UUID id = UUID.randomUUID();
        Instant createdAt = Instant.parse("2026-01-01T10:00:00Z");
        Instant updatedAt = Instant.parse("2026-01-02T12:00:00Z");

        User user = User.reconstitute(id, EMAIL, PASSWORD, ROLE, createdAt, updatedAt);

        assertThat(user.getId()).isEqualTo(id);
        assertThat(user.getEmail()).isEqualTo(EMAIL);
        assertThat(user.getPassword()).isEqualTo(PASSWORD);
        assertThat(user.getRole()).isEqualTo(ROLE);
        assertThat(user.getCreatedAt()).isEqualTo(createdAt);
        assertThat(user.getUpdatedAt()).isEqualTo(updatedAt);
    }
}
