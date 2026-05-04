package dev.webisbrian.vault.infrastructure.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Full-stack integration test for the {@code /secrets} REST endpoints.
 *
 * <p>Starts the real application on a random port with a PostgreSQL instance managed
 * by Testcontainers via the TC JDBC URL in {@code application-test.yml}.
 * Each test cleans the {@code secrets} table in {@code @BeforeEach} because
 * {@code @SpringBootTest} with a real server does not roll back transactions automatically.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class SecretControllerIT {

    // TestRestTemplate auto-includes the context path (/api/v1) — use path relative to it.
    private static final String BASE = "/secrets";

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanUp() {
        // SecretJpaRepository is package-private; use raw SQL to keep the test self-contained.
        jdbcTemplate.execute("DELETE FROM secrets");
    }

    // ── helpers ─────────────────────────────────────────────────────────────

    private Map<String, String> validCreateBody(String name) {
        return Map.of("name", name, "value", "s3cr3t-val", "description", "a description");
    }

    private HttpEntity<Map<String, String>> jsonEntity(Map<String, String> body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(body, headers);
    }

    @SuppressWarnings("unchecked")
    private String createAndGetId(String name) {
        ResponseEntity<Map> response = restTemplate.postForEntity(
                BASE, jsonEntity(validCreateBody(name)), Map.class);
        return (String) response.getBody().get("id");
    }

    // ── tests ────────────────────────────────────────────────────────────────

    @Test
    void should_create_secret_and_return_201() {
        ResponseEntity<Map> response = restTemplate.postForEntity(
                BASE, jsonEntity(validCreateBody("my-api-key")), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().get("id")).isNotNull();
        assertThat(response.getBody().get("name")).isEqualTo("my-api-key");
        assertThat(response.getBody().get("value")).isEqualTo("s3cr3t-val");
        assertThat(response.getBody().get("description")).isEqualTo("a description");
    }

    @Test
    @SuppressWarnings("unchecked")
    void should_return_secret_with_value_when_get_by_id() {
        String id = createAndGetId("get-by-id-key");

        ResponseEntity<Map> response = restTemplate.getForEntity(BASE + "/" + id, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().get("id")).isEqualTo(id);
        assertThat(response.getBody().get("value")).isEqualTo("s3cr3t-val");
    }

    @Test
    @SuppressWarnings("unchecked")
    void should_return_paginated_list_without_values() {
        createAndGetId("key-1");
        createAndGetId("key-2");

        ResponseEntity<Map> response = restTemplate.getForEntity(
                BASE + "?page=0&size=20", Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<Map<String, Object>> items = (List<Map<String, Object>>) response.getBody().get("items");
        assertThat(items).hasSize(2);
        // list endpoint must NOT expose the secret value
        assertThat(items.get(0)).doesNotContainKey("value");
        assertThat(response.getBody().get("totalElements")).isEqualTo(2);
    }

    @Test
    @SuppressWarnings("unchecked")
    void should_update_secret_and_return_200() {
        String id = createAndGetId("original-key");
        Map<String, String> updateBody = Map.of(
                "name", "updated-key", "value", "updated-val", "description", "updated desc");

        ResponseEntity<Map> response = restTemplate.exchange(
                BASE + "/" + id, HttpMethod.PUT, jsonEntity(updateBody), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().get("name")).isEqualTo("updated-key");
        assertThat(response.getBody().get("value")).isEqualTo("updated-val");
    }

    @Test
    void should_delete_secret_and_return_204() {
        String id = createAndGetId("to-delete-key");

        ResponseEntity<Void> response = restTemplate.exchange(
                BASE + "/" + id, HttpMethod.DELETE, null, Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    @SuppressWarnings("unchecked")
    void should_return_404_when_secret_not_found() {
        String randomId = UUID.randomUUID().toString();

        ResponseEntity<Map> response = restTemplate.getForEntity(
                BASE + "/" + randomId, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().get("error")).isEqualTo("NOT_FOUND");
        assertThat(response.getBody().get("message")).asString().contains(randomId);
    }

    @Test
    @SuppressWarnings("unchecked")
    void should_return_409_when_creating_duplicate_name() {
        createAndGetId("duplicate-key");

        ResponseEntity<Map> response = restTemplate.postForEntity(
                BASE, jsonEntity(validCreateBody("duplicate-key")), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().get("error")).isEqualTo("CONFLICT");
    }

    @Test
    @SuppressWarnings("unchecked")
    void should_return_400_when_creating_with_blank_name() {
        Map<String, String> badRequest = Map.of("name", "   ", "value", "val");

        ResponseEntity<Map> response = restTemplate.postForEntity(
                BASE, jsonEntity(badRequest), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().get("error")).isEqualTo("BAD_REQUEST");
    }
}