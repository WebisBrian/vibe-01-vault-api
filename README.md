# Vault API

A REST API for a digital vault — stores and shares secrets (API keys, passwords, tokens) across teams with granular access control, server-side encryption (AES-256-GCM), and a full audit trail.

## Tech stack

- Java 21 / Spring Boot 3.5
- Spring Security + JWT
- Spring Data JPA + PostgreSQL 16
- Flyway migrations
- Swagger / OpenAPI (springdoc)
- Maven
- Docker + Docker Compose
- Testcontainers (integration tests)
- JUnit 5 + Mockito + JaCoCo

## How to run

### Start the database

```bash
docker compose up -d
```

### Run the application (dev profile)

```bash
cd backend
mvn spring-boot:run "-Dspring-boot.run.profiles=dev"
```

Swagger UI: `http://localhost:8080/api/v1/swagger-ui/index.html`

### Run tests

```bash
cd backend
mvn verify
```

Tests use Testcontainers — Docker must be running.

## Environment variables

| Variable      | Default      | Description              |
|---------------|--------------|--------------------------|
| `DB_USERNAME` | `vault_user` | PostgreSQL username      |
| `DB_PASSWORD` | `vault_pass` | PostgreSQL password      |
| `DB_URL`      | —            | Full JDBC URL (prod only)|