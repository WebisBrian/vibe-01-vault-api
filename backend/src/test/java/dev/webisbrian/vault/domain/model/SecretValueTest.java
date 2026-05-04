package dev.webisbrian.vault.domain.model;

import dev.webisbrian.vault.domain.exception.InvalidSecretException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SecretValueTest {

    @Test
    void should_create_when_valid() {
        SecretValue sv = new SecretValue("s3cr3t!");

        assertThat(sv.value()).isEqualTo("s3cr3t!");
    }

    @Test
    void should_throw_when_blank() {
        assertThatThrownBy(() -> new SecretValue("   "))
                .isInstanceOf(InvalidSecretException.class);
    }

    @Test
    void should_throw_when_null() {
        assertThatThrownBy(() -> new SecretValue(null))
                .isInstanceOf(InvalidSecretException.class);
    }
}