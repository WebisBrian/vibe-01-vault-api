# Feature: User Registration (2A)

## Summary

User entity with email/password/role, a public registration endpoint (`POST /auth/register`),
BCrypt password hashing behind a domain port, and an `AdminInitializer` that seeds an ADMIN user
from environment variables on first startup. Role is always `MEMBER` at self-registration;
ADMIN is only created via the initializer. Implements the same hexagonal layers as secret-crud.

---

## Endpoints

All paths are relative to the global context path `/api/v1`.

| Method | Path             | Status | Description               |
|--------|------------------|--------|---------------------------|
| POST   | `/auth/register` | 201    | Register a new MEMBER user |

### Request body

```json
{
  "email": "user@example.com",
  "password": "Password123!"
}
```

Constraints enforced at the web boundary (Bean Validation):
- `email`: not blank, valid email format (`@Email`)
- `password`: not blank, 12–72 characters (`@Size`)

Additional constraint enforced at the domain boundary (`RawPassword`):
- password must satisfy at least 3 of 4 complexity categories: uppercase, lowercase, digit, special character

### 201 response

```json
{
  "id": "uuid",
  "email": "user@example.com",
  "role": "MEMBER",
  "createdAt": "2026-05-04T10:00:00Z",
  "updatedAt": "2026-05-04T10:00:00Z"
}
```

Password is **never** included in any response.

### Error responses

```json
{
  "status": 400,
  "error": "BAD_REQUEST",
  "message": "password: Password must be between 12 and 72 characters",
  "timestamp": "2026-05-04T10:00:00Z",
  "path": "/api/v1/auth/register"
}
```

| Status | Trigger                                                    |
|--------|------------------------------------------------------------|
| 400    | Bean Validation failure (blank, invalid email, short password) |
| 400    | Domain rejection (password fails complexity rule)          |
| 409    | Email already registered (`UserAlreadyExistsException`)    |

---

## Architecture

### Domain layer (`domain/`)

| Class / File                                  | Purpose                                                              |
|-----------------------------------------------|----------------------------------------------------------------------|
| `domain/model/Email`                          | Value object: not blank, ≤ 255 chars, simple `local@domain.tld` regex, normalized to lowercase |
| `domain/model/RawPassword`                    | Value object: not blank, 12–72 chars, ≥ 3 complexity categories. Protective `toString()` |
| `domain/model/EncodedPassword`                | Opaque wrapper for a BCrypt hash — factory method `of()`. Protective `toString()` |
| `domain/model/Role`                           | Enum: `ADMIN`, `MEMBER`, `VIEWER`                                    |
| `domain/model/User`                           | Aggregate root — `create()` factory, `reconstitute()` for rehydration |
| `domain/exception/InvalidUserException`       | Invariant violation on `Email` or `RawPassword` → 400               |
| `domain/exception/UserAlreadyExistsException` | Email already taken → 409                                            |
| `domain/port/in/RegisterUserUseCase`          | Inbound port with inner `Command(email, rawPassword)` record         |
| `domain/port/out/UserRepository`              | Outbound port: `save`, `findByEmail`, `existsByEmail`, `count`       |
| `domain/port/out/PasswordEncoder`             | Outbound port: `encode(RawPassword) → EncodedPassword`               |

### Application layer (`application/service/`)

| Class                  | Implements             | Notes                                           |
|------------------------|------------------------|-------------------------------------------------|
| `RegisterUserService`  | `RegisterUserUseCase`  | Checks uniqueness → encodes → creates → persists |

### Infrastructure layer

| Class / File                                          | Purpose                                                                 |
|-------------------------------------------------------|-------------------------------------------------------------------------|
| `infrastructure/config/ApplicationConfig`             | Updated: added `RegisterUserUseCase` bean (injects `UserRepository` + `PasswordEncoder`) |
| `infrastructure/config/AdminInitializer`              | `@Component`, `ApplicationRunner`: seeds ADMIN from env vars on first startup |
| `infrastructure/security/BcryptPasswordEncoderAdapter`| `@Component` implementing domain `PasswordEncoder` port via BCrypt      |
| `infrastructure/persistence/UserJpaEntity`            | JPA entity mapped to `users` table (package-private)                   |
| `infrastructure/persistence/UserJpaRepository`        | Spring Data JPA interface: `findByEmail`, `existsByEmail` (package-private) |
| `infrastructure/persistence/UserMapper`               | `toJpaEntity` / `toDomain` static converter (package-private)          |
| `infrastructure/persistence/UserPersistenceAdapter`   | `@Component` implementing `UserRepository` domain port                  |
| `infrastructure/web/RegisterRequest`                  | Request DTO with Bean Validation (`@Email`, `@Size`)                    |
| `infrastructure/web/UserResponse`                     | Response DTO — id, email, role, timestamps. No password field           |
| `infrastructure/web/UserWebMapper`                    | `toCommand` / `toResponse` static converter (package-private)           |
| `infrastructure/web/AuthController`                   | `@RestController` at `/auth` — hosts all auth endpoints (login in 2B)  |
| `infrastructure/web/GlobalExceptionHandler`           | Updated: `USER_ALREADY_EXISTS` → 409, `INVALID_USER` → 400             |
| `db/migration/V3__create_users_table.sql`             | Creates `users` table with unique index on `email`                      |

---

## Key decisions

- **Email as sole identifier, no username**: Simpler model. Username is a display-only concern that
  can be added later without changing authentication logic. Email is globally unique by nature.

- **Role enum directly on `User`, not a many-to-many join**: YAGNI — the vault has three fixed roles
  (`ADMIN`, `MEMBER`, `VIEWER`) that are unlikely to become user-configurable. A join table adds
  complexity with no benefit at this stage.

- **`RawPassword` / `EncodedPassword` separation with `PasswordEncoder` port**: The domain defines
  the two value objects and the encoding contract as a port. `RawPassword` enforces complexity rules
  without knowing BCrypt. `EncodedPassword` is an opaque wrapper. The BCrypt adapter lives in
  infrastructure — the domain has zero dependency on Spring Security. Both types override `toString()`
  to return a protected string, preventing accidental leaks in logs.

- **Role always `MEMBER` at registration, not in the request body**: Accepting a role in the request
  would allow clients to self-promote to `ADMIN`. Role assignment is a privileged operation;
  `RegisterUserService` hardcodes `Role.MEMBER` regardless of request content.

- **Admin seeding via `AdminInitializer` + env vars**: A first-run ADMIN is seeded from
  `VAULT_ADMIN_EMAIL` / `VAULT_ADMIN_PASSWORD` environment variables when the `users` table is empty.
  `AdminInitializer` implements `ApplicationRunner`, checks `userRepository.count() == 0`, validates
  env vars are not blank, then creates the user via domain ports. No JPA access directly.
  Idempotent: if any user exists, it exits immediately.

- **Password complexity: min 12 chars, ≥ 3 of 4 categories**: Enforced at the domain level in
  `RawPassword` (uppercase, lowercase, digit, special character). Bean Validation on `RegisterRequest`
  enforces only the length range (12–72) — complexity is a business rule, not a format rule, so it
  belongs in the domain. The two layers are complementary, not redundant.

- **`BcryptPasswordEncoderAdapter` in `infrastructure/security`**: Moved from `infrastructure/config`
  because it is a security adapter, not a wiring concern. Keeps the security package coherent for
  when the JWT filter chain is added in feature 2B.

- **`AuthController` named for future expansion**: Login and token-refresh endpoints (feature 2B)
  will be added to this same controller. Naming it `RegisterController` would require either a
  rename or a poorly named controller later.

---

## How to test

```bash
# Unit tests only — no Docker required
cd backend
mvn test -Dtest="EmailTest,RawPasswordTest,EncodedPasswordTest,UserTest,RegisterUserServiceTest,AdminInitializerTest"

# Full test suite including Testcontainers integration tests — Docker required
cd backend
mvn verify
```

Test breakdown for this feature:

| Layer          | Test class                     | Tests |
|----------------|--------------------------------|-------|
| Domain         | `EmailTest`                    | 10    |
| Domain         | `RawPasswordTest`              | 12    |
| Domain         | `EncodedPasswordTest`          | 4     |
| Domain         | `UserTest`                     | 3     |
| Application    | `RegisterUserServiceTest`      | 4     |
| Application    | `AdminInitializerTest`         | 3     |
| Persistence IT | `UserPersistenceAdapterTest`   | 5     |
| Web IT         | `AuthControllerIT`             | 5     |
| **Total**      |                                | **46**|

---

## Known limitations / TODOs

- `SecurityConfig` is still permit-all — JWT filter chain in feature 2B will restrict endpoints.
- No login or token-refresh endpoints yet — feature 2B.
- No RBAC enforcement — role is stored but not checked on any endpoint — feature 2C.
- `AdminInitializer` does not force a password change on first login.
- `@BeforeEach` cleanup in `AuthControllerIT` deletes from `users` only. When secrets get a
  foreign key to users (future feature), the cleanup order will need adjustment (delete secrets
  before users to respect referential integrity).
