package dev.webisbrian.vault.domain.model;

import dev.webisbrian.vault.domain.exception.InvalidUserException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EncodedPasswordTest {

    @Test
    void should_create_when_valid() {
        EncodedPassword password = EncodedPassword.of("$2a$10$somehashedvalue");

        assertThat(password.value()).isEqualTo("$2a$10$somehashedvalue");
    }

    @Test
    void should_throw_when_null() {
        assertThatThrownBy(() -> EncodedPassword.of(null))
                .isInstanceOf(InvalidUserException.class);
    }

    @Test
    void should_throw_when_blank() {
        assertThatThrownBy(() -> EncodedPassword.of("   "))
                .isInstanceOf(InvalidUserException.class);
    }

    @Test
    void should_return_protected_string_when_toString() {
        EncodedPassword password = EncodedPassword.of("$2a$10$somehashedvalue");

        assertThat(password.toString()).isEqualTo("EncodedPassword[PROTECTED]");
    }
}
