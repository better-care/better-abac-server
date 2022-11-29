package care.better.abac.policy.service;

import care.better.abac.content.AbstractContentSyncStep;
import care.better.abac.content.AppContentCache;
import care.better.abac.content.AppContentRequestContext;
import care.better.abac.dto.content.PlainPolicyDto;
import care.better.abac.jpa.PlainDtoMapper;
import care.better.abac.jpa.entity.EntityWithId;
import care.better.abac.jpa.entity.Policy;
import com.google.common.collect.ImmutableList;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * @author Matic Ribic
 */
public class PolicySyncStep extends AbstractContentSyncStep<PlainPolicyDto, Policy> {
    private final PolicyService policyService;

    public PolicySyncStep(PolicyService policyService) {
        super(policyService);
        this.policyService = policyService;
    }

    @Override
    public List<Policy> getExistingEntities(AppContentRequestContext context, AppContentCache cache) {
        return ImmutableList.copyOf(policyService.findAll());
    }

    @Override
    protected PlainDtoMapper<PlainPolicyDto, Policy> getDtoMapper(AppContentCache cache) {
        return new PolicyDtoMapper(cache);
    }

    @Override
    public Class<PlainPolicyDto> getPlainDtoType() {
        return PlainPolicyDto.class;
    }

    @Override
    public Class<Policy> getEntityType() {
        return Policy.class;
    }

    @Override
    public Set<Class<? extends EntityWithId>> getDependentEntityTypes() {
        return Collections.emptySet();
    }
}
