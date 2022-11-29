package care.better.abac.policy.service;

import care.better.abac.jpa.EntityCrudService;
import care.better.abac.jpa.entity.Policy;

import java.util.Set;

/**
 * @author Matic Ribic
 */
public interface PolicyService extends EntityCrudService<Policy> {

    Iterable<Policy> findAll();

    void registerPolicySync(Set<String> policyNames, boolean refresh);
}
