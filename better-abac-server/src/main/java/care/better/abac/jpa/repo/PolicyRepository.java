package care.better.abac.jpa.repo;

import care.better.abac.jpa.QueryDslRepository;
import care.better.abac.jpa.entity.Policy;
import org.springframework.data.repository.CrudRepository;

/**
 * @author Bostjan Lah
 */
public interface PolicyRepository extends CrudRepository<Policy, Long>, QueryDslRepository<Policy, Long> {
    Policy findByName(String name);
}
