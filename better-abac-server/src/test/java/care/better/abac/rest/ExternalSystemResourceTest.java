package care.better.abac.rest;

import care.better.abac.AbacConfiguration;
import care.better.abac.dto.config.ExternalPolicyDto;
import care.better.abac.dto.config.ExternalPolicyType;
import care.better.abac.dto.config.ExternalSystemDto;
import care.better.abac.dto.config.ExternalSystemInputDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.inject.Inject;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static care.better.abac.rest.ExternalSystemResource.BASE_PATH;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Matic Ribic
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = {AbacConfiguration.class, PolicyExecutionResourceTest.Config.class})
@EnableConfigurationProperties
@EnableAutoConfiguration(exclude = {SecurityAutoConfiguration.class, ManagementWebSecurityAutoConfiguration.class})
@AutoConfigureTestDatabase
@TestPropertySource(properties = "sso.enabled = false")
public class ExternalSystemResourceTest {
    private static final int POLICIES_SIZE = 5;

    @Inject
    private TestRestTemplate restTemplate;

    @Test
    public void createConfig() {
        // given
        ExternalSystemInputDto inputDto = getInputDto();

        // when
        ResponseEntity<Void> createResponse = restTemplate.exchange(BASE_PATH, HttpMethod.POST, new HttpEntity<>(inputDto), Void.class);

        // then
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        List<String> locations = createResponse.getHeaders().get("Location");
        assertThat(locations).isNotNull();
        assertThat(locations).hasSize(1);
        assertThat(locations.get(0)).contains(BASE_PATH);
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Test
    public void updateConfig() {
        // given
        ExternalSystemInputDto inputDto = getInputDto();
        updatePolicyDto(inputDto.getPolicies().stream().filter(policy -> "name 4".equals(policy.getName())).findFirst().get(),
                        4,
                        ExternalPolicyType.QUERY);

        ResponseEntity<Void> createResponse = restTemplate.exchange(BASE_PATH, HttpMethod.POST, new HttpEntity<>(inputDto), Void.class);
        String location = Objects.requireNonNull(createResponse.getHeaders().get("Location")).get(0);

        // when
        inputDto.setName("UPDATED");

        Set<ExternalPolicyDto> policies = new HashSet<>(inputDto.getPolicies());
        policies.removeIf(policy -> "name 1".equals(policy.getName()));
        policies.add(getPolicyDto(10));
        updatePolicyDto(policies.stream().filter(policy -> "name 4".equals(policy.getName())).findFirst().get(),
                        1000,
                        ExternalPolicyType.RULE);

        inputDto.setPolicies(policies);

        ResponseEntity<Void> updateResponse = restTemplate.exchange(location, HttpMethod.PUT, new HttpEntity<>(inputDto), Void.class);

        // then
        assertThat(updateResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        ResponseEntity<ExternalSystemDto> getResponse = restTemplate.exchange(location, HttpMethod.GET, null, ExternalSystemDto.class);

        ExternalSystemDto dto = getResponse.getBody();
        assertThat(dto).isNotNull();
        assertThat(dto.getName()).isEqualTo("UPDATED");
        assertThat(dto.getPolicies()).hasSize(POLICIES_SIZE);

        Optional<ExternalPolicyDto> optionalPolicy1 = dto.getPolicies().stream().filter(policy -> "name 1".equals(policy.getName())).findFirst();
        assertThat(optionalPolicy1).isNotPresent();

        Optional<ExternalPolicyDto> optionalPolicy10 = dto.getPolicies().stream().filter(policy -> "name 10".equals(policy.getName())).findFirst();
        assertThat(optionalPolicy10).isPresent();

        Optional<ExternalPolicyDto> optionalPolicy1000 = dto.getPolicies().stream().filter(policy -> "name 1000".equals(policy.getName())).findFirst();
        assertThat(optionalPolicy1000).isPresent();
        ExternalPolicyDto policy1000 = optionalPolicy1000.get();

        assertThat(policy1000.getName()).isEqualTo("name 1000");
        assertThat(policy1000.getType()).isEqualTo(ExternalPolicyType.RULE);
        assertThat(policy1000.getExternalId()).isNotEmpty();
        assertThat(policy1000.getConfig()).isEqualTo("config 1000");
    }

    @Test
    public void getConfig() {
        // given
        ExternalSystemInputDto inputDto = getInputDto();
        ResponseEntity<Void> createResponse = restTemplate.exchange(BASE_PATH, HttpMethod.POST, new HttpEntity<>(inputDto), Void.class);
        String location = Objects.requireNonNull(createResponse.getHeaders().get("Location")).get(0);

        // when
        ResponseEntity<ExternalSystemDto> getResponse = restTemplate.exchange(location, HttpMethod.GET, null, ExternalSystemDto.class);

        // then
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        ExternalSystemDto dto = getResponse.getBody();
        assertThat(dto).isNotNull();
        assertThat(dto.getPolicies()).hasSize(POLICIES_SIZE);
    }

    @Test
    public void getEmptyConfigWhenNotModified() {
        // given
        ExternalSystemInputDto inputDto = getInputDto();
        ResponseEntity<Void> createResponse = restTemplate.exchange(BASE_PATH, HttpMethod.POST, new HttpEntity<>(inputDto), Void.class);
        String location = Objects.requireNonNull(createResponse.getHeaders().get("Location")).get(0);

        ResponseEntity<ExternalSystemDto> getResponse = restTemplate.exchange(location, HttpMethod.GET, null, ExternalSystemDto.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getBody()).isNotNull();
        String configHash = getResponse.getBody().getConfigHash();

        // when
        ResponseEntity<ExternalSystemDto> getEmptyResponse = restTemplate.exchange(location + "?configHash=" + configHash,
                                                                                   HttpMethod.GET,
                                                                                   null,
                                                                                   ExternalSystemDto.class);

        // then
        assertThat(getEmptyResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_MODIFIED);
        assertThat(getEmptyResponse.getBody()).isNull();
    }

    @Test
    public void getConfigWhenModified() {
        // given
        ExternalSystemInputDto inputDto = getInputDto();
        ResponseEntity<Void> createResponse = restTemplate.exchange(BASE_PATH, HttpMethod.POST, new HttpEntity<>(inputDto), Void.class);
        String location = Objects.requireNonNull(createResponse.getHeaders().get("Location")).get(0);

        ResponseEntity<ExternalSystemDto> getResponse = restTemplate.exchange(location, HttpMethod.GET, null, ExternalSystemDto.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getBody()).isNotNull();
        String configHash = getResponse.getBody().getConfigHash();

        inputDto.setName("UPDATED");
        ResponseEntity<Void> updateResponse = restTemplate.exchange(location, HttpMethod.PUT, new HttpEntity<>(inputDto), Void.class);
        assertThat(updateResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        // when

        ResponseEntity<ExternalSystemDto> getEmptyResponse = restTemplate.exchange(location + "?configHash=" + configHash,
                                                                                   HttpMethod.GET,
                                                                                   null,
                                                                                   ExternalSystemDto.class);

        // then
        assertThat(getEmptyResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getEmptyResponse.getBody()).isNotNull();
        assertThat(getEmptyResponse.getBody().getName()).isEqualTo("UPDATED");
    }

    private ExternalSystemInputDto getInputDto() {
        ExternalSystemInputDto inputDto = new ExternalSystemInputDto();
        inputDto.setName("DEFAULT");
        inputDto.setPolicies(IntStream.range(0, POLICIES_SIZE).mapToObj(this::getPolicyDto).collect(Collectors.toSet()));
        return inputDto;
    }

    public ExternalPolicyDto getPolicyDto(int i) {
        ExternalPolicyDto dto = new ExternalPolicyDto();
        return updatePolicyDto(dto, i, null);
    }

    public ExternalPolicyDto updatePolicyDto(ExternalPolicyDto dto, int i) {
        return updatePolicyDto(dto, i, null);
    }

    public ExternalPolicyDto updatePolicyDto(ExternalPolicyDto dto, int i, ExternalPolicyType type) {
        dto.setName("name " + i);
        dto.setType(type != null ? type : ExternalPolicyType.values()[i % 2]);
        dto.setConfig("config " + i);

        return dto;
    }
}
