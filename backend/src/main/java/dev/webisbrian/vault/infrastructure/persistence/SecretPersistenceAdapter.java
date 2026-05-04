package dev.webisbrian.vault.infrastructure.persistence;

import dev.webisbrian.vault.domain.model.PageResult;
import dev.webisbrian.vault.domain.model.Secret;
import dev.webisbrian.vault.domain.model.SecretName;
import dev.webisbrian.vault.domain.port.out.SecretRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Outbound adapter — implements the domain {@link SecretRepository} port using Spring Data JPA.
 *
 * <p>Acts as the boundary between the hexagonal core and the database. All translation between
 * domain objects and JPA entities is delegated to {@link SecretMapper}, keeping this class
 * focused on orchestration (find → map → return).
 *
 * <p>Annotated with {@code @Component} so Spring discovers it as the concrete {@link SecretRepository}
 * implementation. The application layer depends only on the port interface, never on this class.
 *
 * <p>Layer: infrastructure/persistence — Spring and JPA annotations are intentionally here.
 */
@Component
public class SecretPersistenceAdapter implements SecretRepository {

    private final SecretJpaRepository jpaRepository;

    public SecretPersistenceAdapter(SecretJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Secret save(Secret secret) {
        SecretJpaEntity saved = jpaRepository.save(SecretMapper.toJpaEntity(secret));
        return SecretMapper.toDomain(saved);
    }

    @Override
    public Optional<Secret> findById(UUID id) {
        return jpaRepository.findById(id).map(SecretMapper::toDomain);
    }

    @Override
    public PageResult<Secret> findAll(int page, int size) {
        Page<SecretJpaEntity> jpaPage = jpaRepository.findAll(PageRequest.of(page, size));
        List<Secret> items = jpaPage.getContent().stream()
                .map(SecretMapper::toDomain)
                .toList();
        return new PageResult<>(items, page, size, jpaPage.getTotalElements(), jpaPage.getTotalPages());
    }

    @Override
    public boolean existsByName(SecretName name) {
        return jpaRepository.existsByName(name.value());
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }
}