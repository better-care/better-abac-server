package care.better.abac.rest;

import care.better.abac.AbacConfiguration;
import care.better.abac.dto.PolicyDto;
import care.better.abac.jpa.entity.Policy;
import care.better.abac.jpa.repo.PolicyRepository;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import static care.better.abac.rest.PolicyResource.POLICY_FILE_EXTENSION;
import static care.better.abac.rest.PolicyResourceTest.Config;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Andrej Dolenc
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = {AbacConfiguration.class, Config.class})
@EnableConfigurationProperties
@EnableAutoConfiguration(exclude = {SecurityAutoConfiguration.class, ManagementWebSecurityAutoConfiguration.class})
@AutoConfigureTestDatabase
public class PolicyResourceTest extends AbstractResourceTest {
    static final String BASE_URL = "/rest/v1/admin/policy";

    @Autowired
    private PolicyRepository policyRepository;

    private static final String DEFAULT_POLICY = "hasRelation(ctx.user, 'RELATION_4', ctx.patient)";

    @Test
    public void testPolicyExport() throws IOException {
        ResponseEntity<ByteArrayResource> resource = restTemplate.getForEntity(BASE_URL + "/export", ByteArrayResource.class);
        try (ZipInputStream zipInputStream = new ZipInputStream(resource.getBody().getInputStream())) {
            Set<PolicyDto> policies = new HashSet<>();
            PolicyDto policy;
            //noinspection NestedAssignment
            while ((policy = readPolicyFromZip(zipInputStream)) != null) {
                policies.add(policy);
            }
            assertThat(policies).extracting(PolicyDto::getName).containsExactlyInAnyOrder("TEST_POLICY_1", "TEST_POLICY_2", "TEST_POLICY_3");
            assertThat(policies).extracting(PolicyDto::getPolicy).containsExactlyInAnyOrder("hasRelation(ctx.user, 'RELATION_1', ctx.patient)",
                                                                                            "hasRelation(ctx.user, 'RELATION_2', ctx.patient)",
                                                                                            "hasRelation(ctx.user, 'RELATION_3', ctx.patient)");
        }
    }

    @Test
    public void testPolicyImport() throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        PolicyDto dto = new PolicyDto();
        dto.setPolicy("hasRelation(ctx.user, 'RELATION_4', ctx.patient)");
        dto.setName("TEST_POLICY_4");
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(byteArrayOutputStream, StandardCharsets.UTF_8)) {
            addPolicyToZip(zipOutputStream, dto);
        }
        HttpEntity<?> entity = createMultipart(byteArrayOutputStream);

        restTemplate.exchange(BASE_URL + "/import", HttpMethod.POST, entity, Void.class);
        validateAndDelete(dto);
    }

    @Test
    public void testPolicyImportMultiple() throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        PolicyDto dto = new PolicyDto();
        dto.setPolicy("hasRelation(ctx.user, 'RELATION_4', ctx.patient)");
        dto.setName("TEST_POLICY_4");
        PolicyDto dto1 = new PolicyDto();
        dto1.setPolicy("hasRelation(ctx.user, 'RELATION_4', ctx.patient)");
        dto1.setName("TEST_POLICY_5");
        PolicyDto dto2 = new PolicyDto();
        dto2.setPolicy("hasRelation(ctx.user, 'RELATION_4', ctx.patient)");
        dto2.setName("TEST_POLICY_6");
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(byteArrayOutputStream, StandardCharsets.UTF_8)) {
            addPolicyToZip(zipOutputStream, dto);
            addPolicyToZip(zipOutputStream, dto1);
            addPolicyToZip(zipOutputStream, dto2);
        }
        HttpEntity<?> entity = createMultipart(byteArrayOutputStream);

        restTemplate.exchange(BASE_URL + "/import", HttpMethod.POST, entity, Void.class);
        validateAndDelete(dto);
        validateAndDelete(dto1);
        validateAndDelete(dto2);
    }

    @Test
    public void createPolicy() {
        // given
        String name = "policy_123";

        // when
        PolicyDto createdDto = createEntity(BASE_URL, getPolicyDto(name));

        // then
        assertThat(createdDto.getName()).isEqualTo(name);
        assertThat(createdDto.getPolicy()).isEqualTo(DEFAULT_POLICY);
    }

    @Test
    public void createPolicyIgnoreId() {
        // given
        long idParameter = 1000L;
        PolicyDto policyDto = new PolicyDto(idParameter, "policy_123");
        policyDto.setPolicy(DEFAULT_POLICY);

        // when
        PolicyDto createdDto = createEntity(BASE_URL, policyDto);

        // then
        assertThat(createdDto.getName()).isEqualTo("policy_123");
        assertThat(createdDto.getPolicy()).isEqualTo(DEFAULT_POLICY);
        assertThat(createdDto.getId()).isNotEqualTo(idParameter);
    }

    @Test
    public void updatePolicy() {
        // given
        Long id = createPolicy(getPolicyDto("policy_123")).getId();

        long ignoredId = id + 100L;

        PolicyDto updatedPolicyDto = new PolicyDto(ignoredId, "policy_456");
        updatedPolicyDto.setPolicy("hasRelation(ctx.user, 'RELATION_5', ctx.patient)");

        // when
        PolicyDto updatedDto = updateEntity(BASE_URL, id, updatedPolicyDto);

        // then
        assertThat(updatedDto.getName()).isEqualTo("policy_456");
        assertThat(updatedDto.getPolicy()).isEqualTo("hasRelation(ctx.user, 'RELATION_5', ctx.patient)");
    }

    @Test
    public void updatePolicyForUnknownIdFailed() {
        // given
        PolicyDto policyDto = createPolicy(getPolicyDto("policy_123"));
        long unknownId = policyDto.getId() + 100L;

        // when
        ResponseEntity<PolicyDto> updatedResponse = exchangePut(BASE_URL, unknownId, getPolicyDto("policy_456"));

        // then
        assertThat(updatedResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(updatedResponse.getBody()).isNull();
    }

    @Test
    public void deletePolicy() {
        // given
        Long id = createPolicy(getPolicyDto("policy_123")).getId();

        // when
        ResponseEntity<Void> deleteResponse = exchangeDelete(BASE_URL, id);

        // then
        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void deletePolicyForUnknownIdFailed() {
        // given
        long unknownId = Long.MAX_VALUE;

        // when
        ResponseEntity<Void> deleteResponse = exchangeDelete(BASE_URL, unknownId);

        // then
        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    private PolicyDto getPolicyDto(String name) {
        PolicyDto policyDto = new PolicyDto(null, name);
        policyDto.setPolicy(DEFAULT_POLICY);
        return policyDto;
    }

    private void validateAndDelete(PolicyDto dto) {
        Policy policy = policyRepository.findByName(dto.getName());
        assertThat(dto.getName()).isEqualTo(policy.getName());
        assertThat(dto.getPolicy()).isEqualTo(policy.getPolicy());
        policyRepository.delete(policy);
    }

    private HttpEntity<?> createMultipart(ByteArrayOutputStream byteArrayOutputStream) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        LinkedMultiValueMap<String, String> zipHeaderMap = new LinkedMultiValueMap<>();
        zipHeaderMap.add("Content-disposition", "form-data; name=file; filename=import.zip");
        zipHeaderMap.add("Content-type", "application/octet-stream");
        HttpEntity<byte[]> file = new HttpEntity<>(byteArrayOutputStream.toByteArray(), zipHeaderMap);


        MultiValueMap<String, Object> multipart = new LinkedMultiValueMap<>();
        multipart.add("file", file);
        return new HttpEntity<>(multipart, headers);
    }

    @SneakyThrows
    private PolicyDto readPolicyFromZip(ZipInputStream stream) {
        return ZipUtils.readTextFileFromZip(stream, (fileName, text) -> {
            PolicyDto dto = new PolicyDto();
            dto.setName(StringUtils.removeEnd(fileName, POLICY_FILE_EXTENSION));
            dto.setPolicy(text);
            return dto;
        });
    }

    @SneakyThrows
    private void addPolicyToZip(ZipOutputStream stream, PolicyDto policy) {
        ZipUtils.addTextFileToZip(stream, policy.getName() + POLICY_FILE_EXTENSION, policy.getPolicy());
    }

    @TestConfiguration
    public static class Config {

        @Autowired
        private TestRestTemplate restTemplate;

        @Bean
        public ApplicationRunner initializer() {
            return args -> {
                createPolicy("TEST_POLICY_1", "hasRelation(ctx.user, 'RELATION_1', ctx.patient)");
                createPolicy("TEST_POLICY_2", "hasRelation(ctx.user, 'RELATION_2', ctx.patient)");
                createPolicy("TEST_POLICY_3", "hasRelation(ctx.user, 'RELATION_3', ctx.patient)");
            };
        }

        private PolicyDto createPolicy(String name, String policy) {
            PolicyDto dto = new PolicyDto();
            dto.setPolicy(policy);
            dto.setName(name);
            return restTemplate.postForEntity(BASE_URL, dto, PolicyDto.class).getBody();
        }
    }
}
