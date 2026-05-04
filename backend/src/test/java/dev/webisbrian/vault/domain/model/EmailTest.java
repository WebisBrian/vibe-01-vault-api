package dev.webisbrian.vault.domain.model;

import dev.webisbrian.vault.domain.exception.InvalidUserException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EmailTest {

    @Test
    void should_create_when_valid() {
        Email email = new Email("user@example.com");

        assertThat(email.value()).isEqualTo("user@example.com");
    }

    @Test
    void should_normalize_to_lowercase() {
        Email email = new Email("User@Example.COM");

        assertThat(email.value()).isEqualTo("user@example.com");
    }

    @Test
    void should_throw_when_null() {
        assertThatThrownBy(() -> new Email(null))
                .isInstanceOf(InvalidUserException.class);
    }

    @Test
    void should_throw_when_blank() {
        assertThatThrownBy(() -> new Email("   "))
                .isInstanceOf(InvalidUserException.class);
    }

    @Test
    void should_throw_when_too_long() {
        // 256 chars: 250 + "@b.com"
        String tooLong = "a".repeat(250) + "@b.com";

        assertThatThrownBy(() -> new Email(tooLong))
                .isInstanceOf(InvalidUserException.class);
    }

    @Test
    void should_throw_when_missing_at_sign() {
        assertThatThrownBy(() -> new Email("userexample.com"))
                .isInstanceOf(InvalidUserException.class);
    }

    @Test
    void should_throw_when_nothing_before_at() {
        assertThatThrownBy(() -> new Email("@example.com"))
                .isInstanceOf(InvalidUserException.class);
    }

    @Test
    void should_throw_when_nothing_after_at() {
        assertThatThrownBy(() -> new Email("user@"))
                .isInstanceOf(InvalidUserException.class);
    }

    @Test
    void should_throw_when_no_dot_in_domain() {
        assertThatThrownBy(() -> new Email("user@localhost"))
                .isInstanceOf(InvalidUserException.class);
    }

    @Test
    void should_be_equal_when_same_value() {
        Email a = new Email("user@example.com");
        Email b = new Email("user@example.com");

        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }
}
