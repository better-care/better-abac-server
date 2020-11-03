package care.better.abac.external;

import care.better.abac.AbacConfiguration;
import care.better.abac.dto.config.ExternalPolicyDto;
import care.better.abac.dto.config.ExternalPolicyType;
import care.better.abac.dto.config.ExternalSystemDto;
import care.better.abac.dto.config.ExternalSystemInputDto;
import care.better.abac.jpa.entity.ExternalSystemEntity;
import care.better.abac.rest.PolicyExecutionResourceTest;
import care.better.abac.rest.client.ValidationException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import javax.inject.Inject;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

/**
 * @author Matic Ribic
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = {AbacConfiguration.class, PolicyExecutionResourceTest.Config.class})
@EnableAutoConfiguration(exclude = SecurityAutoConfiguration.class)
@AutoConfigureTestDatabase
@DirtiesContext
public class ExternalSystemServiceTest {

    @Inject
    private ExternalSystemService externalSystemService;

    private ExternalSystemInputDto inputDto;

    @Before
    public void setUp() {
        ExternalPolicyDto policyDto = new ExternalPolicyDto();
        policyDto.setName("Name");
        policyDto.setExternalId("ExternalId");
        policyDto.setType(ExternalPolicyType.QUERY);
        policyDto.setConfig("Config");

        inputDto = new ExternalSystemInputDto();
        inputDto.setName("Client");
        inputDto.setAbacRestBaseUrl("http://ignored.better.care");
        inputDto.setPolicies(Collections.singleton(policyDto));
    }

    @Test
    public void createConfig() {
        // when
        ExternalSystemEntity savedConfig = null;
        try {
            savedConfig = externalSystemService.createConfig(inputDto, false);
        } catch (ValidationException e) {
            fail(e.getMessage());
        }

        // then
        assertThat(savedConfig).isNotNull();
        assertThat(externalSystemService.getConfigDto(savedConfig.getExternalId())).isNotNull();
    }

    @Test
    public void createConfigAndFailOnNotify() {
        // when
        ExternalSystemEntity savedConfig = null;
        try {
            savedConfig = externalSystemService.createConfigAndNotify(inputDto, false);
        } catch (ValidationException e) {
            fail(e.getMessage());
        }

        // then
        assertThat(savedConfig).isNotNull();
        assertThat(externalSystemService.getConfigDto(savedConfig.getExternalId())).isNotNull();
    }

    @Test
    public void updateConfig() {
        // given
        ExternalSystemEntity createdConfig = null;
        try {
            createdConfig = externalSystemService.createConfig(inputDto, false);
        } catch (ValidationException e) {
            fail(e.getMessage());
        }
        inputDto.setName("UPDATED");

        // when
        ExternalSystemEntity updatedConfig = null;
        try {
            updatedConfig = externalSystemService.updateConfig(createdConfig.getExternalId(), inputDto, false);
        } catch (ValidationException e) {
            fail(e.getMessage());
        }

        // then
        assertThat(updatedConfig).isNotNull();
        assertThat(updatedConfig.getName()).isEqualTo("UPDATED");
        ExternalSystemDto updatedConfigDto = externalSystemService.getConfigDto(updatedConfig.getExternalId());
        assertThat(updatedConfigDto).isNotNull();
        assertThat(updatedConfigDto.getName()).isEqualTo("UPDATED");
    }

    @Test
    public void updateConfigAndFailOnNotify() {
        // given
        ExternalSystemEntity createdConfig = null;
        try {
            createdConfig = externalSystemService.createConfig(inputDto, false);
        } catch (ValidationException e) {
            fail(e.getMessage());
        }
        inputDto.setName("UPDATED");

        // when
        ExternalSystemEntity updatedConfig = null;
        try {
            updatedConfig = externalSystemService.updateConfigAndNotify(createdConfig.getExternalId(), inputDto, false);
        } catch (ValidationException e) {
            fail(e.getMessage());
        }

        // then
        assertThat(updatedConfig).isNotNull();
        assertThat(updatedConfig.getName()).isEqualTo("UPDATED");
        ExternalSystemDto updatedConfigDto = externalSystemService.getConfigDto(updatedConfig.getExternalId());
        assertThat(updatedConfigDto).isNotNull();
        assertThat(updatedConfigDto.getName()).isEqualTo("UPDATED");
    }
}