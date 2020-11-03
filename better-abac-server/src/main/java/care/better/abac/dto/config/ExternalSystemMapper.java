package care.better.abac.dto.config;

import care.better.abac.jpa.entity.ExternalPolicyEntity;
import care.better.abac.jpa.entity.ExternalSystemEntity;

import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Matic Ribic
 */
public class ExternalSystemMapper {
    private final ExternalPolicyMapper policyMapper;

    public ExternalSystemMapper(ExternalPolicyMapper policyMapper) {
        this.policyMapper = policyMapper;
    }

    public ExternalSystemDto toDto(ExternalSystemEntity entity) {
        ExternalSystemDto dto = new ExternalSystemDto();
        dto.setName(entity.getName());
        dto.setExternalId(entity.getExternalId());
        dto.setConfigHash(entity.getConfigHash());
        dto.setAbacRestBaseUrl(entity.getAbacRestBaseUrl());
        dto.setValidationStatus(entity.getValidationStatus());

        dto.setPolicies(entity.getPolicies().stream().map(policyMapper::toDto).collect(Collectors.toSet()));
        return dto;
    }

    public ExternalSystemInputDto toInputDto(ExternalSystemDto sourceDto) {
        ExternalSystemInputDto dto = new ExternalSystemInputDto();
        dto.setName(sourceDto.getName());
        dto.setAbacRestBaseUrl(sourceDto.getAbacRestBaseUrl());

        dto.setPolicies(sourceDto.getPolicies());
        return dto;
    }

    public ExternalSystemEntity toEntity(ExternalSystemInputDto inputDto, ExternalSystemValidationStatus validationStatus) {
        ExternalSystemEntity entity = new ExternalSystemEntity();
        entity.setExternalId(UUID.randomUUID().toString());

        updateEntity(entity, inputDto, validationStatus);

        return entity;
    }

    public void updateEntity(ExternalSystemEntity targetEntity, ExternalSystemInputDto inputDto, ExternalSystemValidationStatus validationStatus) {
        targetEntity.setName(inputDto.getName());
        targetEntity.setAbacRestBaseUrl(inputDto.getAbacRestBaseUrl());
        targetEntity.setValidationStatus(validationStatus);

        Map<String, ExternalPolicyEntity> targetPoliciesByExternalIds = targetEntity.getPolicies()
                .stream()
                .collect(Collectors.toMap(ExternalPolicyEntity::getExternalId, Function.identity()));

        inputDto.getPolicies().forEach(sourcePolicyDto -> {
            ExternalPolicyEntity targetPolicy = targetPoliciesByExternalIds.remove(sourcePolicyDto.getExternalId());
            if (targetPolicy != null) {
                policyMapper.updateEntity(targetPolicy, sourcePolicyDto);
            } else {
                targetEntity.getPolicies().add(policyMapper.toEntity(sourcePolicyDto, targetEntity));
            }
        });

        targetEntity.getPolicies().removeAll(targetPoliciesByExternalIds.values());

        targetEntity.setConfigHash(policyMapper.calculateConfigHash(targetEntity.getPolicies()));
    }
}
