package dev.webisbrian.vault.domain.model;

import dev.webisbrian.vault.domain.exception.InvalidSecretException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SecretNameTest {

    @Test
    void should_create_when_valid() {
        SecretName name = new SecretName("my-api-key");

        assertThat(name.value()).isEqualTo("my-api-key");
    }

    @Test
    void should_throw_when_blank() {
        assertThatThrownBy(() -> new SecretName("   "))
                .isInstanceOf(InvalidSecretException.class);
    }

    @Test
    void should_throw_when_null() {
        assertThatThrownBy(() -> new SecretName(null))
                .isInstanceOf(InvalidSecretException.class);
    }

    @Test
    void should_throw_when_too_long() {
        String tooLong = "a".repeat(256);

        assertThatThrownBy(() -> new SecretName(tooLong))
                .isInstanceOf(InvalidSecretException.class);
    }

    @Test
    void should_be_equal_when_same_value() {
        SecretName a = new SecretName("my-key");
        SecretName b = new SecretName("my-key");

        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }
}