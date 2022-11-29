package care.better.abac.policy.service;

import care.better.abac.content.AppContentCache;
import care.better.abac.content.AppContentRequestContext;
import care.better.abac.dto.content.PlainPolicyDto;
import care.better.abac.jpa.AbstractPlainDtoMapper;
import care.better.abac.jpa.entity.Policy;

import java.util.Objects;

/**
 * @author Matic Ribic
 */
public class PolicyDtoMapper extends AbstractPlainDtoMapper<PlainPolicyDto, Policy> {

    public PolicyDtoMapper(AppContentCache cache) {
        super(cache);
    }

    @Override
    public PlainPolicyDto toPlainDto(Policy entity) {
        PlainPolicyDto dto = new PlainPolicyDto();
        dto.setName(entity.getName());
        dto.setPolicy(entity.getPolicy());
        return dto;
    }

    @Override
    public Policy createEntity(PlainPolicyDto dto, boolean dryRun) {
        Policy policy = new Policy(dryRun ? generateNextId(Policy.class) : null);
        policy.setName(dto.getName());
        policy.setPolicy(dto.getPolicy());
        return policy;
    }

    @Override
    public boolean doKeysMatch(PlainPolicyDto dto, Policy entity) {
        return entity.getName().equals(dto.getName());
    }

    @Override
    public boolean isChanged(PlainPolicyDto dto, Policy entity) {
        return !Objects.equals(entity.getPolicy(), dto.getPolicy());
    }

    @Override
    public void validateDto(PlainPolicyDto dto, AppContentRequestContext context) {
    }

    @Override
    protected Class<Policy> getEntityType() {
        return Policy.class;
    }
}
