package care.better.abac.content;

import care.better.abac.jpa.EntityCrudService;
import care.better.abac.jpa.entity.EntityWithId;

import java.util.Optional;

/**
 * @author Matic Ribic
 */
public class NoopEntityCrudService<T extends EntityWithId> implements EntityCrudService<T> {

    @Override
    public T create(T entity) {
        return entity;
    }

    @Override
    public Optional<T> update(Long id, T entity) {
        return Optional.of(entity);
    }

    @Override
    public void deleteById(Long id) {
    }
}
