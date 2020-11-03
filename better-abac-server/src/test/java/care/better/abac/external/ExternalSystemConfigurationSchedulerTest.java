package care.better.abac.external;

import care.better.abac.AbacConfiguration;
import care.better.abac.dto.config.ExternalPolicyDto;
import care.better.abac.dto.config.ExternalPolicyType;
import care.better.abac.dto.config.ExternalSystemInputDto;
import care.better.abac.dto.config.ExternalSystemValidationStatus;
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
public class ExternalSystemConfigurationSchedulerTest {

    @Inject
    private ExternalSystemConfigurationScheduler externalSystemConfigurationScheduler;

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
    public void validate() {
        // given
        ExternalSystemEntity savedConfig = null;
        try {
            savedConfig = externalSystemService.createConfig(inputDto, false);
        } catch (ValidationException e) {
            fail(e.getMessage());
        }

        // preconditions
        assertThat(savedConfig).isNotNull();
        assertThat(savedConfig.getValidationStatus()).isEqualTo(ExternalSystemValidationStatus.UNKNOWN);
        assertThat(externalSystemService.getConfigListToValidate()).hasSize(1);

        // when
        externalSystemConfigurationScheduler.validate();

        // then
        assertThat(externalSystemService.getConfigListToValidate()).isEmpty();
        assertThat(externalSystemService.getConfigDto(savedConfig.getExternalId()).getValidationStatus()).isEqualTo(ExternalSystemValidationStatus.INVALID);
    }
}