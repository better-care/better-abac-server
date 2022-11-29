package care.better.abac.policy.service.impl;

import care.better.abac.jpa.entity.Policy;
import care.better.abac.jpa.repo.PolicyRepository;
import care.better.abac.policy.service.PolicyExecutionService;
import care.better.abac.policy.service.PolicyService;
import com.google.common.base.Preconditions;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

/**
 * @author Bostjan Lah
 */
@Transactional
public class PolicyServiceImpl implements PolicyService {
    private final PolicyRepository policyRepository;
    private final PolicyExecutionService pdlPolicyService;

    public PolicyServiceImpl(PolicyRepository policyRepository, PolicyExecutionService pdlPolicyService) {
        this.policyRepository = policyRepository;
        this.pdlPolicyService = pdlPolicyService;
    }

    @Override
    public Iterable<Policy> findAll() {
        return policyRepository.findAll();
    }

    @Override
    public Policy create(Policy policy) {
        Preconditions.checkArgument(policy.getId() == null, "Parameter policy cannot be created because it has set ID already.");
        return policyRepository.save(policy);
    }

    @Override
    public Optional<Policy> update(Long id, Policy submittedPolicy) {
        return policyRepository.findById(id).map(entity -> {
            String policyName = submittedPolicy.getName();
            entity.setName(policyName);
            entity.setPolicy(submittedPolicy.getPolicy());

            registerPolicySync(Collections.singleton(policyName), true);
            return entity;
        });
    }

    @Override
    public void deleteById(Long id) {
        // throw an exception as it's thrown in CrudRepository.deleteById(Long)
        Policy entity = policyRepository.findById(id)
                .orElseThrow(() -> new EmptyResultDataAccessException(String.format("No %s entity with id %s exists!", Policy.class.getName(), id), 1));
        String policyName = entity.getName();

        policyRepository.delete(entity);
        registerPolicySync(Collections.singleton(policyName), false);
    }

    @Override
    public void registerPolicySync(Set<String> policyNames, boolean refresh) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
            @Override
            public void afterCommit() {
                policyNames.forEach(p -> pdlPolicyService.policyUpdated(p, refresh));
            }
        });
    }
}
