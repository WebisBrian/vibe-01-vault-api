package dev.webisbrian.vault.domain.model;

import dev.webisbrian.vault.domain.exception.InvalidUserException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RawPasswordTest {

    @Test
    void should_create_when_valid() {
        RawPassword password = new RawPassword("Password123!");

        assertThat(password.value()).isEqualTo("Password123!");
    }

    @Test
    void should_accept_minimum_length() {
        // 12 chars — exact lower boundary, satisfies 4 categories
        RawPassword password = new RawPassword("Abcdefghij1!");

        assertThat(password.value()).isEqualTo("Abcdefghij1!");
    }

    @Test
    void should_accept_maximum_length() {
        // 72 chars — exact upper boundary (BCrypt limit), satisfies 4 categories
        String maxLength = "Aa1!" + "a".repeat(68);
        RawPassword password = new RawPassword(maxLength);

        assertThat(password.value()).isEqualTo(maxLength);
    }

    @Test
    void should_throw_when_null() {
        assertThatThrownBy(() -> new RawPassword(null))
                .isInstanceOf(InvalidUserException.class);
    }

    @Test
    void should_throw_when_blank() {
        assertThatThrownBy(() -> new RawPassword("   "))
                .isInstanceOf(InvalidUserException.class);
    }

    @Test
    void should_throw_when_too_short() {
        // 11 chars — one below minimum (length check fires before complexity)
        assertThatThrownBy(() -> new RawPassword("Abcdefghi1!"))
                .isInstanceOf(InvalidUserException.class);
    }

    @Test
    void should_throw_when_too_long() {
        // 73 chars — one above maximum (length check fires before complexity)
        assertThatThrownBy(() -> new RawPassword("a".repeat(73)))
                .isInstanceOf(InvalidUserException.class);
    }

    @Test
    void should_throw_when_only_one_category() {
        // 13 lowercase chars — fails complexity (1 of 4 categories)
        assertThatThrownBy(() -> new RawPassword("abcdefghijklm"))
                .isInstanceOf(InvalidUserException.class);
    }

    @Test
    void should_throw_when_only_two_categories() {
        // lowercase + digits only, 12 chars — fails complexity (2 of 4 categories)
        assertThatThrownBy(() -> new RawPassword("abcdefghij12"))
                .isInstanceOf(InvalidUserException.class);
    }

    @Test
    void should_accept_three_categories() {
        // lowercase + uppercase + digits, 12 chars
        RawPassword password = new RawPassword("abcdefghiJ12");

        assertThat(password.value()).isEqualTo("abcdefghiJ12");
    }

    @Test
    void should_accept_four_categories() {
        // lowercase + uppercase + digits + special, 12 chars
        RawPassword password = new RawPassword("abcdefghJ1!x");

        assertThat(password.value()).isEqualTo("abcdefghJ1!x");
    }

    @Test
    void should_return_protected_string_when_toString() {
        RawPassword password = new RawPassword("Password123!");

        assertThat(password.toString()).isEqualTo("RawPassword[PROTECTED]");
    }
}
