package dev.webisbrian.vault.infrastructure.persistence;

import dev.webisbrian.vault.domain.model.Secret;
import dev.webisbrian.vault.domain.model.SecretName;
import dev.webisbrian.vault.domain.model.SecretValue;

/**
 * Bidirectional converter between the domain {@link Secret} aggregate and
 * its persistence representation {@link SecretJpaEntity}.
 *
 * <p>Uses {@link Secret#reconstitute} when loading from the DB so that no UUID or timestamp
 * is regenerated — the stored identity and timestamps are preserved exactly as persisted.
 *
 * <p>Layer: infrastructure/persistence — knows both domain and JPA types; belongs here only.
 */
class SecretMapper {

    private SecretMapper() {}

    /** Converts a domain Secret to a JPA entity ready for persistence. */
    static SecretJpaEntity toJpaEntity(Secret secret) {
        return new SecretJpaEntity(
                secret.getId(),
                secret.getName().value(),
                secret.getValue().value(),
                secret.getDescription(),
                secret.getCreatedAt(),
                secret.getUpdatedAt()
        );
    }

    /** Rehydrates a domain Secret from a JPA entity loaded from the database. */
    static Secret toDomain(SecretJpaEntity entity) {
        return Secret.reconstitute(
                entity.getId(),
                new SecretName(entity.getName()),
                new SecretValue(entity.getValue()),
                entity.getDescription(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}