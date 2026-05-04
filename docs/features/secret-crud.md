# Feature: Secret CRUD

## Summary

Full CRUD for `Secret` entities — create, retrieve, list (paginated), update, and delete secrets
stored in the vault. Each secret holds a unique name, a sensitive value, and an optional description.
Implements hexagonal architecture: domain → application → infrastructure.

---

## Endpoints

All paths are relative to the global context path `/api/v1`.

| Method | Path              | Status | Description                                    |
|--------|-------------------|--------|------------------------------------------------|
| POST   | `/secrets`        | 201    | Create a new secret (response includes value)  |
| GET    | `/secrets/{id}`   | 200    | Get a secret by ID (response includes value)   |
| GET    | `/secrets`        | 200    | List secrets paginated — **no value in items** |
| PUT    | `/secrets/{id}`   | 200    | Replace all mutable fields                     |
| DELETE | `/secrets/{id}`   | 204    | Delete a secret                                |

### Request shapes

**POST / PUT body:**
```json
{
  "name": "my-api-key",
  "value": "s3cr3t-payload",
  "description": "optional free text"
}
```

**GET /{id} / POST / PUT response:**
```json
{
  "id": "uuid",
  "name": "my-api-key",
  "value": "s3cr3t-payload",
  "description": "optional free text",
  "createdAt": "2026-04-29T14:00:00Z",
  "updatedAt": "2026-04-29T14:00:00Z"
}
```

**GET list response:**
```json
{
  "items": [{ "id": "...", "name": "...", "description": "...", "createdAt": "...", "updatedAt": "..." }],
  "page": 0,
  "size": 20,
  "totalElements": 5,
  "totalPages": 1
}
```
Note: list items intentionally omit `value` to avoid bulk exposure of sensitive data.

**Error response (all non-2xx):**
```json
{
  "status": 404,
  "error": "NOT_FOUND",
  "message": "Secret not found with id: <uuid>",
  "timestamp": "2026-04-29T14:30:00Z",
  "path": "/api/v1/secrets/<uuid>"
}
```

---

## Architecture

### Domain layer (`domain/`)

| Class / File                              | Purpose                                                    |
|-------------------------------------------|------------------------------------------------------------|
| `domain/model/SecretName`                 | Value object: name ≤ 255 chars, not blank                  |
| `domain/model/SecretValue`                | Value object: plaintext payload, not blank                 |
| `domain/model/Secret`                     | Aggregate root — `create()` factory, `reconstitute()`, `update()` |
| `domain/model/PageResult<T>`              | Generic paginated result, no Spring dependency             |
| `domain/exception/InvalidSecretException` | Invariant violation → 400                                  |
| `domain/exception/SecretNotFoundException`| Resource not found → 404                                   |
| `domain/exception/SecretAlreadyExistsException` | Name collision → 409                                 |
| `domain/port/in/CreateSecretUseCase`      | Inbound port with inner `Command` record                   |
| `domain/port/in/GetSecretUseCase`         | Inbound port — find by UUID                                |
| `domain/port/in/ListSecretsUseCase`       | Inbound port — paginated list                              |
| `domain/port/in/UpdateSecretUseCase`      | Inbound port with inner `Command` record                   |
| `domain/port/in/DeleteSecretUseCase`      | Inbound port — delete by UUID                              |
| `domain/port/out/SecretRepository`        | Outbound port — persistence contract                       |

### Application layer (`application/service/`)

One class per use case — a single class implementing all five interfaces would cause a Java method
signature collision (`execute(UUID)` defined with incompatible return types by `GetSecretUseCase`
and `DeleteSecretUseCase`).

| Class                  | Implements              |
|------------------------|-------------------------|
| `CreateSecretService`  | `CreateSecretUseCase`   |
| `GetSecretService`     | `GetSecretUseCase`      |
| `ListSecretsService`   | `ListSecretsUseCase`    |
| `UpdateSecretService`  | `UpdateSecretUseCase`   |
| `DeleteSecretService`  | `DeleteSecretUseCase`   |

### Infrastructure layer

| Class / File                                 | Purpose                                                    |
|----------------------------------------------|------------------------------------------------------------|
| `infrastructure/config/ApplicationConfig`    | `@Configuration` that creates use-case service beans      |
| `infrastructure/persistence/SecretJpaEntity` | JPA entity mapped to `secrets` table                      |
| `infrastructure/persistence/SecretJpaRepository` | Spring Data JPA interface (package-private)           |
| `infrastructure/persistence/SecretMapper`    | `toJpaEntity` / `toDomain` static converter               |
| `infrastructure/persistence/SecretPersistenceAdapter` | `@Component` implementing `SecretRepository`   |
| `infrastructure/web/CreateSecretRequest`     | Request DTO with Bean Validation                           |
| `infrastructure/web/UpdateSecretRequest`     | Request DTO with Bean Validation                           |
| `infrastructure/web/SecretResponse`          | List response — no value field                             |
| `infrastructure/web/SecretDetailResponse`    | Single-resource response — includes value                  |
| `infrastructure/web/PageResponse<T>`         | Paginated response wrapper                                 |
| `infrastructure/web/SecretWebMapper`         | DTO ↔ domain command converter (package-private)          |
| `infrastructure/web/SecretController`        | `@RestController` at `/secrets`                           |
| `infrastructure/web/GlobalExceptionHandler`  | Updated: routes `DomainException` error codes to HTTP status |
| `db/migration/V2__create_secrets_table.sql`  | Creates `secrets` table with unique index on `name`        |

---

## Key decisions

- **`TIMESTAMP WITH TIME ZONE` in migration**: Hibernate 6 (Spring Boot 3.x) maps `java.time.Instant`
  to `TIMESTAMP WITH TIME ZONE` on PostgreSQL. Using bare `TIMESTAMP` would fail
  `ddl-auto: validate` at startup.

- **One service class per use case**: `GetSecretUseCase.execute(UUID)` and
  `DeleteSecretUseCase.execute(UUID)` have incompatible return types — a single class cannot
  implement both. Each use case is a separate class injected by `ApplicationConfig`.

- **`ApplicationConfig` as composition root**: Application services are plain Java (no `@Service`).
  `ApplicationConfig` in the infrastructure layer creates them as Spring beans and injects the
  `SecretRepository` port. This keeps the application layer free of framework annotations.

- **`SecretJpaRepository` is package-private**: Only `SecretPersistenceAdapter` (in the same package)
  may use it. Nothing outside `infrastructure.persistence` should access the Spring Data interface directly.

- **List endpoint omits value**: `GET /secrets` returns `SecretResponse` (no value). Only
  single-resource endpoints return `SecretDetailResponse`. Prevents accidental bulk value exposure.

- **`JdbcTemplate` cleanup in IT test**: `SecretControllerIT` uses `JdbcTemplate.execute("DELETE FROM secrets")`
  for `@BeforeEach` cleanup. Importing `SecretJpaRepository` from a different package is not possible
  (it is package-private by design).

- **`GlobalExceptionHandler` exclusion removed** from JaCoCo: `SecretControllerIT` now exercises the
  domain exception handler (404, 409) and the validation handler (400).

- **`reconstitute()` factory method on `Secret`**: Used exclusively by `SecretMapper.toDomain()` to
  rehydrate domain objects from persistence without regenerating the UUID or timestamps.

---

## How to test

```bash
# Unit tests only (no Docker needed)
cd backend
mvn test

# Full test suite including integration tests + JaCoCo coverage check (Docker required)
cd backend
mvn verify
```

Expected: 36 tests total — 21 unit tests (domain + application), 6 persistence adapter tests,
8 controller IT tests, 1 smoke IT test. JaCoCo coverage check: PASS.

---

## Known limitations / TODOs

- `SecurityConfig` is still permit-all — replace with JWT filter chain in the auth feature.
- No encryption of `value` at rest — the field is stored as plaintext. Encryption (AES-256-GCM)
  is deferred to a dedicated feature.
- No audit trail — created-by / updated-by tracking is deferred to the access-control feature.