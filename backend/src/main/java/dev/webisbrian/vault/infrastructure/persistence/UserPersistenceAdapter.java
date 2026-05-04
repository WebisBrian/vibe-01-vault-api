package dev.webisbrian.vault.infrastructure.persistence;

import dev.webisbrian.vault.domain.model.Email;
import dev.webisbrian.vault.domain.model.User;
import dev.webisbrian.vault.domain.port.out.UserRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Outbound adapter — implements the domain {@link UserRepository} port using Spring Data JPA.
 *
 * <p>Acts as the boundary between the hexagonal core and the database. All translation between
 * domain objects and JPA entities is delegated to {@link UserMapper}, keeping this class
 * focused on orchestration (find → map → return).
 *
 * <p>Annotated with {@code @Component} so Spring discovers it as the concrete {@link UserRepository}
 * implementation. The application layer depends only on the port interface, never on this class.
 *
 * <p>Layer: infrastructure/persistence — Spring and JPA annotations are intentionally here.
 */
@Component
public class UserPersistenceAdapter implements UserRepository {

    private final UserJpaRepository jpaRepository;

    public UserPersistenceAdapter(UserJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public User save(User user) {
        UserJpaEntity saved = jpaRepository.save(UserMapper.toJpaEntity(user));
        return UserMapper.toDomain(saved);
    }

    @Override
    public Optional<User> findByEmail(Email email) {
        return jpaRepository.findByEmail(email.value()).map(UserMapper::toDomain);
    }

    @Override
    public boolean existsByEmail(Email email) {
        return jpaRepository.existsByEmail(email.value());
    }

    @Override
    public long count() {
        return jpaRepository.count();
    }
}
