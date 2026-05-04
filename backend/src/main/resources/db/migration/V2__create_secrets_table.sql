-- Hibernate 6 (Spring Boot 3.x) maps java.time.Instant to TIMESTAMP WITH TIME ZONE on PostgreSQL.
-- Using TIMESTAMP (without time zone) would fail the 'ddl-auto: validate' check at startup.
CREATE TABLE secrets
(
    id          UUID PRIMARY KEY,
    name        VARCHAR(255)             NOT NULL,
    value       TEXT                     NOT NULL,
    description TEXT,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at  TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE UNIQUE INDEX idx_secrets_name ON secrets (name);