package dev.webisbrian.vault.infrastructure.persistence;

import dev.webisbrian.vault.domain.model.Email;
import dev.webisbrian.vault.domain.model.EncodedPassword;
import dev.webisbrian.vault.domain.model.Role;
import dev.webisbrian.vault.domain.model.User;
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

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Slice test for {@link UserPersistenceAdapter} against a real PostgreSQL instance
 * managed by Testcontainers via the TC JDBC URL in {@code application-test.yml}.
 *
 * <p>{@code @DataJpaTest} wraps each test in a rolled-back transaction, so tests start
 * with an empty {@code users} table without explicit cleanup.
 * {@code FlywayAutoConfiguration} is imported explicitly because {@code @DataJpaTest}
 * does not include Flyway in its slice by default.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@ImportAutoConfiguration(FlywayAutoConfiguration.class)
class UserPersistenceAdapterTest {

    @Autowired
    private UserJpaRepository jpaRepository;

    @Autowired
    private TestEntityManager em;

    private UserPersistenceAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new UserPersistenceAdapter(jpaRepository);
    }

    private User saveUser(String email) {
        return adapter.save(User.create(
                new Email(email),
                EncodedPassword.of("$2a$10$hash"),
                Role.MEMBER
        ));
    }

    @Test
    void should_save_and_find_user_by_email() {
        User saved = saveUser("user@example.com");

        em.flush();
        em.clear();

        Optional<User> found = adapter.findByEmail(new Email("user@example.com"));

        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(saved.getId());
        assertThat(found.get().getEmail().value()).isEqualTo("user@example.com");
        assertThat(found.get().getPassword().value()).isEqualTo("$2a$10$hash");
        assertThat(found.get().getRole()).isEqualTo(Role.MEMBER);
        assertThat(found.get().getCreatedAt()).isNotNull();
        assertThat(found.get().getUpdatedAt()).isNotNull();
    }

    @Test
    void should_return_empty_when_email_not_found() {
        Optional<User> found = adapter.findByEmail(new Email("nonexistent@example.com"));

        assertThat(found).isEmpty();
    }

    @Test
    void should_return_true_when_email_exists() {
        saveUser("user@example.com");
        em.flush();

        assertThat(adapter.existsByEmail(new Email("user@example.com"))).isTrue();
    }

    @Test
    void should_return_false_when_email_does_not_exist() {
        assertThat(adapter.existsByEmail(new Email("nonexistent@example.com"))).isFalse();
    }

    @Test
    void should_count_users() {
        saveUser("user1@example.com");
        saveUser("user2@example.com");
        em.flush();

        assertThat(adapter.count()).isEqualTo(2);
    }
}
