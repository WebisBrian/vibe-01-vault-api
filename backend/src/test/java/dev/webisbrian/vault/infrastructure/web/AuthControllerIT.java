package dev.webisbrian.vault.infrastructure.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Full-stack integration test for the {@code /auth} REST endpoints.
 *
 * <p>Starts the real application on a random port with a PostgreSQL instance managed
 * by Testcontainers via the TC JDBC URL in {@code application-test.yml}.
 * Each test cleans the {@code users} table in {@code @BeforeEach} because
 * {@code @SpringBootTest} with a real server does not roll back transactions automatically.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class AuthControllerIT {

    private static final String BASE = "/auth";

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanUp() {
        // UserJpaRepository is package-private; use raw SQL to keep the test self-contained.
        jdbcTemplate.execute("DELETE FROM users");
    }

    // ── helpers ─────────────────────────────────────────────────────────────

    private Map<String, String> validBody() {
        return Map.of("email", "user@example.com", "password", "Password123!");
    }

    private HttpEntity<Map<String, String>> jsonEntity(Map<String, String> body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(body, headers);
    }

    // ── tests ────────────────────────────────────────────────────────────────

    @Test
    @SuppressWarnings("unchecked")
    void should_register_user_and_return_201() {
        ResponseEntity<Map> response = restTemplate.postForEntity(
                BASE + "/register", jsonEntity(validBody()), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().get("id")).isNotNull();
        assertThat(response.getBody().get("email")).isEqualTo("user@example.com");
        assertThat(response.getBody().get("role")).isEqualTo("MEMBER");
        assertThat(response.getBody().get("createdAt")).isNotNull();
        assertThat(response.getBody().get("updatedAt")).isNotNull();
        assertThat(response.getBody()).doesNotContainKey("password");
    }

    @Test
    @SuppressWarnings("unchecked")
    void should_return_400_when_email_invalid() {
        Map<String, String> body = Map.of("email", "not-an-email", "password", "Password123!");

        ResponseEntity<Map> response = restTemplate.postForEntity(
                BASE + "/register", jsonEntity(body), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().get("error")).isEqualTo("BAD_REQUEST");
    }

    @Test
    @SuppressWarnings("unchecked")
    void should_return_400_when_password_too_short() {
        // 11 chars — one below the 12-char minimum
        Map<String, String> body = Map.of("email", "user@example.com", "password", "Abcdefghi1!");

        ResponseEntity<Map> response = restTemplate.postForEntity(
                BASE + "/register", jsonEntity(body), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().get("error")).isEqualTo("BAD_REQUEST");
    }

    @Test
    @SuppressWarnings("unchecked")
    void should_return_400_when_password_lacks_complexity() {
        // 13 lowercase chars — passes @Size but fails domain complexity (1 of 4 categories)
        Map<String, String> body = Map.of("email", "user@example.com", "password", "abcdefghijklm");

        ResponseEntity<Map> response = restTemplate.postForEntity(
                BASE + "/register", jsonEntity(body), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().get("error")).isEqualTo("BAD_REQUEST");
    }

    @Test
    @SuppressWarnings("unchecked")
    void should_return_409_when_email_already_taken() {
        restTemplate.postForEntity(BASE + "/register", jsonEntity(validBody()), Map.class);

        ResponseEntity<Map> response = restTemplate.postForEntity(
                BASE + "/register", jsonEntity(validBody()), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().get("error")).isEqualTo("CONFLICT");
    }
}
