package care.better.abac.jpa;

import care.better.abac.jpa.entity.EntityWithId;
import care.better.abac.jpa.repo.EntityCrudRepository;
import com.google.common.base.Preconditions;

import java.util.Optional;

/**
 * @author Matic Ribic
 */
public class RepositoryAsEntityCrudService<T extends EntityWithId> implements EntityCrudService<T> {
    private final EntityCrudRepository<T> repository;

    public RepositoryAsEntityCrudService(EntityCrudRepository<T> repository) {
        this.repository = repository;
    }

    @Override
    public T create(T entity) {
        Preconditions.checkArgument(entity.getId() == null,
                                    "Parameter " + entity.getClass().getSimpleName() + " cannot be created because it has set ID already.");
        return repository.save(entity);
    }

    @Override
    public Optional<T> update(Long id, T entity) {
        return repository.update(id, entity);
    }

    @Override
    public void deleteById(Long id) {
        repository.deleteById(id);
    }
}
