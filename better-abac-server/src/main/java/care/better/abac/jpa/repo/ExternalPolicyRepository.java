package care.better.abac.jpa.repo;

import care.better.abac.jpa.QueryDslRepository;
import care.better.abac.jpa.entity.ExternalPolicyEntity;
import org.springframework.data.repository.CrudRepository;

/**
 * @author Matic Ribic
 */
public interface ExternalPolicyRepository extends CrudRepository<ExternalPolicyEntity, Long>, QueryDslRepository<ExternalPolicyEntity, Long> {
}
