package care.better.abac.dto.config;

import care.better.abac.jpa.entity.ExternalPolicyEntity;
import care.better.abac.jpa.entity.ExternalSystemEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Matic Ribic
 */
public class ExternalPolicyMapperTest {
    private ExternalPolicyMapper mapper;

    @BeforeEach
    public void setUp() {
        mapper = new ExternalPolicyMapper();
    }

    @Test
    public void toDto() {
        // given
        ExternalPolicyEntity entity = new ExternalPolicyEntity();
        entity.setName("Name");
        entity.setExternalId("ExternalId");
        entity.setType(ExternalPolicyType.QUERY);
        entity.setConfig("Config");

        // when
        ExternalPolicyDto dto = mapper.toDto(entity);

        // then
        assertThat(dto.getName()).isEqualTo("Name");
        assertThat(dto.getExternalId()).isEqualTo("ExternalId");
        assertThat(dto.getType()).isEqualTo(ExternalPolicyType.QUERY);
        assertThat(dto.getConfig()).isEqualTo("Config");
    }

    @Test
    public void toEntity() {
        // given
        ExternalSystemEntity systemEntity = new ExternalSystemEntity();
        ExternalPolicyDto dto = new ExternalPolicyDto();
        dto.setName("Name");
        dto.setType(ExternalPolicyType.QUERY);
        dto.setConfig("Config");

        // when
        ExternalPolicyEntity entity = mapper.toEntity(dto, systemEntity);

        // then
        assertThat(entity.getName()).isEqualTo("Name");
        assertThat(entity.getType()).isEqualTo(ExternalPolicyType.QUERY);
        assertThat(entity.getConfig()).isEqualTo("Config");
        assertThat(entity.getExternalSystem()).isEqualTo(systemEntity);
    }

    @Test
    public void updateEntity() {
        // given
        ExternalPolicyDto dto = new ExternalPolicyDto();
        dto.setName("Name");
        dto.setType(ExternalPolicyType.QUERY);
        dto.setConfig("Config");

        ExternalPolicyEntity entity = new ExternalPolicyEntity();

        // when
        mapper.updateEntity(entity, dto);

        // then
        assertThat(entity.getName()).isEqualTo("Name");
        assertThat(entity.getType()).isEqualTo(ExternalPolicyType.QUERY);
        assertThat(entity.getConfig()).isEqualTo("Config");
    }
}
