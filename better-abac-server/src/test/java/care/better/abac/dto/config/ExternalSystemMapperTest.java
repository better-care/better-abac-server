package care.better.abac.dto.config;


import care.better.abac.jpa.entity.ExternalPolicyEntity;
import care.better.abac.jpa.entity.ExternalSystemEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Matic Ribic
 */
public class ExternalSystemMapperTest {
    private ExternalSystemMapper mapper;

    @BeforeEach
    public void setUp() {
        mapper = new ExternalSystemMapper(new ExternalPolicyMapper());
    }

    @Test
    public void toDto() {
        // given
        ExternalPolicyEntity policyEntity = new ExternalPolicyEntity();
        policyEntity.setName("Name");
        policyEntity.setExternalId("ExternalId");
        policyEntity.setType(ExternalPolicyType.QUERY);
        policyEntity.setConfig("Config");

        ExternalSystemEntity systemEntity = new ExternalSystemEntity();
        systemEntity.setName("Client");
        systemEntity.setExternalId("ID");
        systemEntity.setConfigHash("HASH");
        systemEntity.setAbacRestBaseUrl("URL");
        systemEntity.setValidationStatus(ExternalSystemValidationStatus.VALID);
        systemEntity.setPolicies(Collections.singleton(policyEntity));

        // when
        ExternalSystemDto systemDto = mapper.toDto(systemEntity);

        // then
        assertThat(systemDto.getName()).isEqualTo("Client");
        assertThat(systemDto.getExternalId()).isEqualTo("ID");
        assertThat(systemDto.getConfigHash()).isEqualTo("HASH");
        assertThat(systemDto.getAbacRestBaseUrl()).isEqualTo("URL");
        assertThat(systemDto.getValidationStatus()).isEqualTo(ExternalSystemValidationStatus.VALID);

        assertThat(systemDto.getPolicies()).hasSize(1);
        ExternalPolicyDto policyDto = systemDto.getPolicies().iterator().next();

        assertThat(policyDto.getName()).isEqualTo("Name");
        assertThat(policyDto.getExternalId()).isEqualTo("ExternalId");
        assertThat(policyDto.getType()).isEqualTo(ExternalPolicyType.QUERY);
        assertThat(policyDto.getConfig()).isEqualTo("Config");
    }

    @Test
    public void toEntity() {
        // given
        ExternalPolicyDto policyDto = new ExternalPolicyDto();
        policyDto.setName("Name");
        policyDto.setExternalId("ExternalId");
        policyDto.setType(ExternalPolicyType.QUERY);
        policyDto.setConfig("Config");

        ExternalSystemInputDto inputDto = new ExternalSystemInputDto();
        inputDto.setName("Client");
        inputDto.setAbacRestBaseUrl("URL");
        inputDto.setPolicies(Collections.singleton(policyDto));

        // when
        ExternalSystemEntity systemEntity = mapper.toEntity(inputDto, ExternalSystemValidationStatus.VALID);

        // then
        assertThat(systemEntity.getName()).isEqualTo("Client");
        assertThat(systemEntity.getExternalId()).isNotEmpty();
        assertThat(systemEntity.getConfigHash()).isNotEmpty();
        assertThat(systemEntity.getAbacRestBaseUrl()).isEqualTo("URL");
        assertThat(systemEntity.getValidationStatus()).isEqualTo(ExternalSystemValidationStatus.VALID);

        assertThat(systemEntity.getPolicies()).hasSize(1);
        ExternalPolicyEntity policyEntity = systemEntity.getPolicies().iterator().next();

        assertThat(policyEntity.getName()).isEqualTo("Name");
        assertThat(policyEntity.getExternalId()).isNotEmpty();
        assertThat(policyEntity.getExternalId()).isNotEqualTo("ExternalId");

        assertThat(policyEntity.getType()).isEqualTo(ExternalPolicyType.QUERY);
        assertThat(policyEntity.getConfig()).isEqualTo("Config");
    }

    @Test
    public void updateEntity() {
        // given
        ExternalPolicyDto policyDto = new ExternalPolicyDto();
        policyDto.setName("Name");
        policyDto.setExternalId("ExternalId");
        policyDto.setType(ExternalPolicyType.QUERY);
        policyDto.setConfig("Config");

        ExternalSystemInputDto inputDto = new ExternalSystemInputDto();
        inputDto.setName("Client");
        inputDto.setAbacRestBaseUrl("URL");
        inputDto.setPolicies(Collections.singleton(policyDto));

        ExternalSystemEntity systemEntity = new ExternalSystemEntity();
        systemEntity.setExternalId("ID");

        // when
        mapper.updateEntity(systemEntity, inputDto, ExternalSystemValidationStatus.VALID);

        // then
        assertThat(systemEntity.getName()).isEqualTo("Client");
        assertThat(systemEntity.getExternalId()).isEqualTo("ID");
        assertThat(systemEntity.getConfigHash()).isNotEmpty();
        assertThat(systemEntity.getAbacRestBaseUrl()).isEqualTo("URL");
        assertThat(systemEntity.getValidationStatus()).isEqualTo(ExternalSystemValidationStatus.VALID);

        assertThat(systemEntity.getPolicies()).hasSize(1);
        ExternalPolicyEntity policyEntity = systemEntity.getPolicies().iterator().next();

        assertThat(policyEntity.getName()).isEqualTo("Name");
        assertThat(policyEntity.getExternalId()).isNotEmpty();
        assertThat(policyEntity.getExternalId()).isNotEqualTo("ExternalId");

        assertThat(policyEntity.getType()).isEqualTo(ExternalPolicyType.QUERY);
        assertThat(policyEntity.getConfig()).isEqualTo("Config");
    }
}
