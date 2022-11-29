package care.better.abac.jpa;

import care.better.abac.jpa.entity.EntityWithId;

import java.util.Optional;

/**
 * @author Matic Ribic
 */
public interface EntityCrudService<T extends EntityWithId> {

    T create(T entity);

    Optional<T> update(Long id, T entity);

    void deleteById(Long id);
}
