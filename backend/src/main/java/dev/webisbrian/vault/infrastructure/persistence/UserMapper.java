package dev.webisbrian.vault.infrastructure.persistence;

import dev.webisbrian.vault.domain.model.Email;
import dev.webisbrian.vault.domain.model.EncodedPassword;
import dev.webisbrian.vault.domain.model.User;

/**
 * Bidirectional converter between the domain {@link User} aggregate and
 * its persistence representation {@link UserJpaEntity}.
 *
 * <p>Uses {@link User#reconstitute} when loading from the DB so that no UUID or timestamp
 * is regenerated — the stored identity and timestamps are preserved exactly as persisted.
 *
 * <p>Layer: infrastructure/persistence — knows both domain and JPA types; belongs here only.
 */
class UserMapper {

    private UserMapper() {}

    /** Converts a domain User to a JPA entity ready for persistence. */
    static UserJpaEntity toJpaEntity(User user) {
        return new UserJpaEntity(
                user.getId(),
                user.getEmail().value(),
                user.getPassword().value(),
                user.getRole(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }

    /** Rehydrates a domain User from a JPA entity loaded from the database. */
    static User toDomain(UserJpaEntity entity) {
        return User.reconstitute(
                entity.getId(),
                new Email(entity.getEmail()),
                EncodedPassword.of(entity.getPassword()),
                entity.getRole(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
