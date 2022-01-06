package care.better.abac.dto.config;

import care.better.abac.jpa.entity.ExternalPolicyEntity;
import care.better.abac.jpa.entity.ExternalSystemEntity;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Matic Ribic
 */
public class ExternalPolicyMapper {

    public ExternalPolicyDto toDto(ExternalPolicyEntity entity) {
        ExternalPolicyDto dto = new ExternalPolicyDto();
        dto.setName(entity.getName());
        dto.setExternalId(entity.getExternalId());

        dto.setType(entity.getType());
        dto.setPhase(Optional.ofNullable(entity.getPhase()).orElse(ExternalPolicyPhase.PRE_PROCESS));
        dto.setConfig(entity.getConfig());

        return dto;
    }

    public ExternalPolicyEntity toEntity(ExternalPolicyDto sourceDto, ExternalSystemEntity systemEntity) {
        ExternalPolicyEntity targetEntity = new ExternalPolicyEntity();
        targetEntity.setExternalSystem(systemEntity);
        targetEntity.setExternalId(UUID.randomUUID().toString());

        updateEntity(targetEntity, sourceDto);

        return targetEntity;
    }

    public void updateEntity(ExternalPolicyEntity targetEntity, ExternalPolicyDto sourceDto) {
        targetEntity.setName(sourceDto.getName());

        targetEntity.setType(sourceDto.getType());
        targetEntity.setPhase(Optional.ofNullable(sourceDto.getPhase()).orElse(ExternalPolicyPhase.PRE_PROCESS));
        targetEntity.setConfig(sourceDto.getConfig());
    }

    public String calculateConfigHash(Set<ExternalPolicyEntity> policies) {
        Object[] values = policies.stream().sorted(Comparator.comparing(ExternalPolicyEntity::getExternalId)).map(this::getPolicyValuesForHash).toArray();
        return Integer.toHexString(Objects.hash(values));
    }

    private List<String> getPolicyValuesForHash(ExternalPolicyEntity policy) {
        return Stream.of(policy.getName(),
                         policy.getType(),
                         policy.getPhase(),
                         policy.getConfig()).map(String::valueOf).collect(Collectors.toList());
    }
}
