package care.better.abac.jpa.repo;

import care.better.abac.jpa.entity.EntityWithId;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.Optional;

/**
 * @author Matic Ribic
 */
@NoRepositoryBean
public interface EntityCrudRepository<T extends EntityWithId> extends CrudRepository<T, Long> {

    Optional<T> update(Long id, T entity);
}
