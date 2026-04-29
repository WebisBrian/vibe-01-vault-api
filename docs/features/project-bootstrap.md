# Feature: Project Bootstrap

## Summary

Sets up the monorepo skeleton, Spring Boot project structure, hexagonal package layout, security baseline, exception handling infrastructure, application profiles, Docker Compose, and CI pipeline. No business features — purely infrastructure and scaffolding.

## Endpoints

None. This feature contains no REST endpoints.

## Architecture

### Classes created

| Layer          | Class / File                                      | Purpose                                      |
|----------------|---------------------------------------------------|----------------------------------------------|
| Entry point    | `VaultApiApplication`                             | Spring Boot entry point (renamed from `VaultApplication`) |
| Domain         | `domain/exception/DomainException`                | Abstract base for all business exceptions    |
| Infrastructure | `infrastructure/config/SecurityConfig`            | Permit-all security config (temporary)       |
| Infrastructure | `infrastructure/web/ApiErrorResponse`             | Java record for consistent error JSON        |
| Infrastructure | `infrastructure/web/GlobalExceptionHandler`       | `@RestControllerAdvice` mapping exceptions → HTTP |

### Packages created (empty, preserved with `.gitkeep`)

- `domain/model/` — entities and value objects
- `domain/port/in/` — inbound port interfaces (use-case contracts)
- `domain/port/out/` — outbound port interfaces (repository contracts)
- `application/service/` — use case implementations
- `infrastructure/persistence/` — JPA adapters
- `frontend/` — React SPA (future)

## Key decisions

- **Testcontainers TC JDBC URL** in `application-test.yml` (`jdbc:tc:postgresql:16:///vault_db`): no manual container management in test code — Testcontainers starts/stops the DB automatically when the JDBC connection is opened.
- **Swagger disabled in test profile**: `springdoc.swagger-ui.enabled: false` and `springdoc.api-docs.enabled: false` are set in `application-test.yml`. Avoids loading the OpenAPI context during tests, which reduces startup time and prevents irrelevant failures on doc generation.
- **JaCoCo exclusions**: four classes are excluded from the 80% coverage check at bootstrap — `VaultApiApplication` (entry point), `ApiErrorResponse` (pure data record, no logic), `DomainException` (abstract base, subclasses carry the logic), and `GlobalExceptionHandler` (handler methods require live HTTP requests; will be covered by feature-level IT tests). Exclusions should be revisited and narrowed as feature coverage grows.
- **CI has no PostgreSQL service container**: integration tests manage their own DB via Testcontainers — `ubuntu-latest` runners include Docker, so no external service container is needed. The workflow is `checkout → setup-java → mvn verify`.
- **`server.servlet.context-path: /api/v1`**: All endpoints (including future ones) are rooted at `/api/v1`. Configured in `application.yml` (common) so every profile inherits it.
- **`SecurityConfig` is temporary**: Permit-all, CSRF disabled. Will be replaced with JWT authentication in the auth feature.
- **`maven-failsafe-plugin`**: added explicitly to run `*IT.java` tests during the `integration-test` phase. Without it, surefire silently skips IT classes — they compile but never execute. Failsafe binds to `integration-test` + `verify`, so `mvn verify` runs both unit tests (surefire) and integration tests (failsafe).

## How to test

```bash
# Docker must be running (Testcontainers requires it)
cd backend
mvn verify
```

Expected: `VaultApiApplicationIT` passes, `VaultApplicationTests` passes, JaCoCo report generated under `target/site/jacoco/`.

## Known limitations / TODOs

- `SecurityConfig` permits all requests — replace with JWT filter chain in the auth feature.
- `GlobalExceptionHandler` is excluded from the JaCoCo check at bootstrap — handler methods require live HTTP requests to be covered. The exclusion must be removed once the first feature's IT tests exercise the error paths.
- `NoHandlerFoundException` handler: Spring Boot 3.5 may prefer `NoResourceFoundException`; the handler will be revisited if 404 responses come back as the Spring default error page instead of the custom JSON format.