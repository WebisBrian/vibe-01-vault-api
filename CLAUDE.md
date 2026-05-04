# CLAUDE.md — Vault API

## Your Role

You are the **implementation engine** for this project. You write code, tests, configs, and migrations.

You do **NOT** make architectural decisions. Architecture, data modeling, design patterns, and technical trade-offs are decided in Claude Web (chat). You receive clear instructions on **what** to build and **how** to structure it. If instructions are ambiguous or missing, **ask** — do not guess.

---

## Session Workflow

Each Claude Code session covers **one feature** (one `feature/*` branch). Do not `/clear` unless efficiency degrades.

### Feature Documentation

Maintain a living doc for each feature in `docs/features/` (e.g. `docs/features/secret-crud.md`). Update it **as you implement**, not just at the end. The doc must include:

- **Summary**: what the feature does (2-3 lines).
- **Endpoints** (if any): method, path, request/response shape.
- **Architecture**: which classes were created/modified, in which layer.
- **Key decisions**: any non-obvious choice made during implementation (with rationale from Claude Web).
- **How to test**: commands to run, what to verify.
- **Known limitations / TODOs**: anything deferred or incomplete.

This doc is the re-entry point for future work on the feature — debugging, extending, onboarding.

### Commit Strategy

At the end of implementation (or at logical checkpoints), **propose commits** — do not commit silently.

Rules:
- Group files by **logical unit**: domain + its tests = one commit, application + its tests = another, infrastructure + its tests = another. Config and migrations can be their own commit.
- Each commit must compile and tests must pass independently. No broken intermediate states.
- Propose the full list of commits with their messages and file groups. The developer validates before committing.
- Use Conventional Commits format (see Git Conventions below).

Example for a CRUD feature:
```
1. feat(domain): add Secret entity and value objects
   - src/main/.../domain/model/Secret.java
   - src/main/.../domain/model/SecretName.java
   - src/test/.../domain/model/SecretTest.java

2. feat(application): add CreateSecret use case
   - src/main/.../application/usecase/CreateSecretUseCase.java
   - src/test/.../application/usecase/CreateSecretUseCaseTest.java

3. feat(infrastructure): add Secret REST adapter and JPA repository
   - src/main/.../infrastructure/web/SecretController.java
   - src/main/.../infrastructure/persistence/SecretJpaRepository.java
   - src/test/.../infrastructure/web/SecretControllerIT.java

4. docs: add secret-crud feature documentation
   - docs/features/secret-crud.md
```

---

## Project

REST API for a digital vault — stores and shares secrets (API keys, passwords, tokens) across teams with granular access control, server-side encryption (AES-256-GCM), and full audit trail.

---

## Stack

- **Java 21**
- **Spring Boot 3.5.14**
- Spring Security + JWT
- Spring Data JPA + PostgreSQL
- Flyway (migrations)
- Swagger / OpenAPI (springdoc-openapi)
- Maven
- Docker + Docker Compose
- Testcontainers (integration tests)
- JUnit 5 + Mockito
- JaCoCo (coverage > 80%)
- GitHub Actions (CI/CD)

**Group ID**: `dev.webisbrian.vault`
**Base package**: `dev.webisbrian.vault`

### Monorepo Structure

```
vault/                          ← git root
├── backend/                    ← Spring Boot project (pom.xml here)
│   └── src/main/java/dev/webisbrian/vault/
├── frontend/                   ← React (future)
├── docs/
│   └── features/
├── docker-compose.yml          ← root-level orchestration
├── CLAUDE.md
└── README.md
```

All Maven commands run from `backend/`. All paths to source code are relative to `backend/`.

---

## Architecture — Hexagonal (Ports & Adapters)

Three layers with strict dependency rules:

```
infrastructure → application → domain
```

- **domain**: entities, value objects, business rules, port interfaces. **Zero framework imports.** No Spring, no JPA, no Jakarta annotations. Pure Java only.
- **application**: use cases / services orchestrating domain logic. Depends only on domain.
- **infrastructure**: adapters (REST controllers, JPA repositories, security, external services). Depends on domain + application. This is the only layer that touches frameworks.

Dependencies always point inward. Never the reverse.

---

## Validation — Three Layers

Each layer protects itself:

1. **Infrastructure (web adapter)** — Format/syntactic validation. Bean Validation (`@NotBlank`, `@Email`, `@Size`) on request DTOs. Triggered by `@Valid`.
2. **Domain (entities, value objects)** — Business rule validation. Pure Java, no annotations. Objects enforce invariants via factory methods, constructors, guard clauses. Domain must be valid by construction.
3. **Application (use cases)** — Orchestration validation. Precondition checks: does the resource exist? Does the user have permission? Is this operation allowed?

---

## Exception Handling

### Domain exceptions (`domain/exception/`)

All business exceptions extend a base `DomainException` class. Pure Java, no framework dependency.

- `DomainException` — abstract base class with message and error code.
- Specific exceptions are created per feature as needed (e.g. `SecretNotFoundException`, `InvalidSecretException`).
- Domain exceptions carry business meaning — they do NOT know about HTTP.

### Error response format

All API errors return a consistent JSON structure:

```json
{
  "status": 404,
  "error": "NOT_FOUND",
  "message": "Secret not found with id: xxx",
  "timestamp": "2026-04-29T14:30:00Z",
  "path": "/api/v1/secrets/xxx"
}
```

### GlobalExceptionHandler (`infrastructure/web/`)

A `@RestControllerAdvice` that translates exceptions to HTTP responses:

| Exception type                     | HTTP status | When                          |
|------------------------------------|-------------|-------------------------------|
| `DomainException` (subclasses)     | Varies      | Business rule violation        |
| `MethodArgumentNotValidException`  | 400         | Bean Validation failure        |
| `MissingServletRequestParameter`   | 400         | Missing required param         |
| `HttpMessageNotReadableException`  | 400         | Malformed JSON                 |
| `NoHandlerFoundException`          | 404         | Unknown endpoint               |
| `Exception` (fallback)             | 500         | Unexpected error               |

The domain does NOT know about HTTP. The handler is the only place where exception → status code mapping happens.

---

## TDD — Mandatory

Every feature follows Red-Green-Refactor:

1. Write the test first.
2. Watch it fail (red).
3. Implement the minimum to pass (green).
4. Refactor.

Never write implementation code without a corresponding test.

### Test types

- **Unit tests** (`*Test.java`): JUnit 5 + Mockito. Domain logic and application services.
- **Integration tests** (`*IT.java`): `@SpringBootTest` + Testcontainers. Full endpoint testing with real DB.
- **Slice tests**: `@WebMvcTest` for controllers, `@DataJpaTest` for repositories.
- **Naming**: `should_<expected>_when_<condition>` (e.g. `should_throw_exception_when_secret_name_is_blank`)

---

## Code Conventions

### Naming

| Element             | Convention          | Example                              |
|---------------------|---------------------|--------------------------------------|
| Classes             | PascalCase          | `SecretService`                      |
| Methods / variables | camelCase           | `findByName`                         |
| Constants           | UPPER_SNAKE_CASE    | `MAX_SECRET_LENGTH`                  |
| Packages            | lowercase, no `_`   | `dev.webisbrian.vault.domain.model`  |
| DB tables / columns | snake_case          | `access_policies`, `created_at`      |
| REST endpoints      | kebab-case          | `/api/v1/access-policies`            |
| Test methods        | should_when pattern | `should_return_secret_when_user_has_access` |

### Rules

- **Language**: all code, comments, Javadoc, commit messages, branch names in **English**.
- **DTOs**: use Java records when possible.
- **No business logic in controllers** — controllers delegate to application services only.
- **Every endpoint** must have Swagger annotations (`@Operation`, `@ApiResponse`).
- **No secrets, passwords, or keys in versioned files.** Use environment variables. `.env` files are `.gitignore`'d.

### Code Comments

The developer is learning — comments are essential for comprehension and future maintenance.

- **Every class**: a Javadoc header explaining its role, which layer it belongs to, and why it exists.
- **Every public method**: a brief Javadoc explaining what it does, not how.
- **Non-obvious logic**: inline comments (`//`) explaining the *why*, not the *what*. If the code needs a comment to explain *what* it does, the code should be refactored first.
- **Architectural choices**: when a pattern is applied (port, adapter, factory, guard clause), a short comment referencing *why* this pattern is used here.
- **No noise comments**: do not comment getters, setters, trivial constructors, or self-explanatory one-liners.

---

## Git Conventions

### Commits — Conventional Commits

```
feat: add secret creation endpoint
fix: handle null encryption key gracefully
test: add integration tests for access policy
docs: update API documentation
chore: configure JaCoCo plugin
refactor: extract encryption logic to dedicated service
```

### Branches

- `main` — stable, production-ready
- `develop` — integration branch
- `feature/*` — one branch per feature, from `develop`
- `fix/*` — bug fixes

Small, atomic commits. Avoid monolithic commits.

---

## Environments

| Profile | Database              | Config file              | Notes                                  |
|---------|-----------------------|--------------------------|----------------------------------------|
| dev     | Docker Compose (PG)   | `application-dev.yml`    | Debug logging, Swagger UI enabled      |
| test    | Testcontainers (PG)   | `application-test.yml`   | Ephemeral DB, triggered by `mvn verify`|
| prod    | Env vars              | `application-prod.yml`   | No debug, no Swagger UI, no secrets    |

---

## Logging

**Stack**: SLF4J + Logback (Spring Boot default). No extra dependency needed.

### Levels by profile

| Profile | `dev.webisbrian.vault` | Spring / Hibernate | Root   |
|---------|------------------------|--------------------|--------|
| dev     | DEBUG                  | INFO               | INFO   |
| test    | WARN                   | WARN               | WARN   |
| prod    | INFO                   | WARN               | WARN   |

### Usage in code

- `logger.debug()` — execution flow, method entry/exit, intermediate values. Dev only.
- `logger.info()` — significant business events (secret created, access granted, user logged in).
- `logger.warn()` — abnormal but recoverable situations (access denied, unexpected data).
- `logger.error()` — unrecoverable errors, caught exceptions.

### Rules

- Use `private static final Logger logger = LoggerFactory.getLogger(ClassName.class);` — never `System.out.println()`.
- **Never log sensitive data**: secrets, passwords, tokens, encryption keys.
- Keep log messages concise and actionable. Include identifiers (IDs, usernames) but not payloads.

---

## Dependencies

**Never add a dependency without prior approval.** If you think a new library is needed, stop and ask. This includes Maven dependencies, plugins, and any external library.

---

## Reminders

- Domain layer: zero framework annotations — if you see `@Entity`, `@Column`, `@Autowired` in domain, it's wrong.
- No business logic in controllers — if a controller has an `if` that isn't about HTTP concerns, it's wrong.
- Tests come first — if you write implementation without a test, rewrite.
- When in doubt, ask. Do not make structural decisions on your own.
- Favor a code-run-fix cycle over long upfront deliberation. Write the code, run the tests, fix failures. Do not try to predict every issue before coding.