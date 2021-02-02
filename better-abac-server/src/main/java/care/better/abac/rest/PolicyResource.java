package care.better.abac.rest;

import care.better.core.Opt;
import care.better.abac.dto.PolicyDto;
import care.better.abac.jpa.entity.Policy;
import care.better.abac.jpa.repo.PolicyRepository;
import care.better.abac.policy.service.PolicyService;
import lombok.NonNull;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * @author Bostjan Lah
 */
@Component
@RestController
@RequestMapping("/rest/v1/admin/policy")
@Transactional
public class PolicyResource {
    public static final String POLICY_FILE_EXTENSION = ".pdl";

    private final PolicyRepository policyRepository;
    private final PolicyService pdlPolicyService;

    @Autowired
    public PolicyResource(@NonNull PolicyRepository policyRepository, @NonNull PolicyService pdlPolicyService) {
        this.policyRepository = policyRepository;
        this.pdlPolicyService = pdlPolicyService;
    }

    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<PolicyDto> findAll() {
        return StreamSupport.stream(policyRepository.findAll().spliterator(), false)
                .map(this::map)
                .collect(Collectors.toList());
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public PolicyDto findOne(@PathVariable("id") Long id) {
        Policy policy = policyRepository.findById(id).get();
        return policy == null ? null : map(policy);
    }

    @RequestMapping(value = "/name/{name}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public PolicyDto findOneByName(@PathVariable("name") String name) {
        Policy policy = policyRepository.findByName(name);
        return policy == null ? null : map(policy);
    }

    @RequestMapping(method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PolicyDto> create(@RequestBody PolicyDto dto) {
        Policy policy = new Policy();
        map(dto, policy);
        Policy saved = policyRepository.save(policy);
        return ResponseEntity.created(linkTo(methodOn(PolicyResource.class).findOne(saved.getId())).toUri()).body(map(saved));
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PolicyDto> update(@PathVariable("id") Long id, @RequestBody PolicyDto dto) {
        Policy policy = policyRepository.findById(id).get();
        map(dto, policy);

        String policyName = policy.getName();
        registerPolicySync(Collections.singleton(policyName), true);

        return ResponseEntity.ok(map(policy));
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        Policy policy = policyRepository.findById(id).get();
        String policyName = policy.getName();

        policyRepository.deleteById(id);
        registerPolicySync(Collections.singleton(policyName), false);
        return ResponseEntity.ok().build();
    }

    @RequestMapping(value = "/export", method = RequestMethod.GET)
    public ResponseEntity<Resource> exportPolicies() throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(byteArrayOutputStream, StandardCharsets.UTF_8)) {
            policyRepository.findAll().forEach(policy -> addPolicyToZip(zipOutputStream, policy));
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=policies.zip")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(new ByteArrayResource(byteArrayOutputStream.toByteArray()));
    }

    @RequestMapping(value = "/import", method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> importPolicies(@RequestParam(value = "file") MultipartFile file, HttpServletRequest request) throws IOException {
        Set<String> synchronizations = new HashSet<>();
        try (ZipInputStream zipInputStream = new ZipInputStream(file.getInputStream())) {
            Policy policy;
            //noinspection NestedAssignment
            while ((policy = readPolicyFromZip(zipInputStream)) != null) {
                synchronizations.add(policy.getName());
                policyRepository.save(policy);
            }
        }
        registerPolicySync(synchronizations, true);
        return ResponseEntity.noContent().build();
    }

    @SneakyThrows
    private void addPolicyToZip(ZipOutputStream stream, Policy policy) {
        ZipUtils.addTextFileToZip(stream, policy.getName() + POLICY_FILE_EXTENSION, policy.getPolicy());
    }

    @SneakyThrows
    private Policy readPolicyFromZip(ZipInputStream stream) {
        return ZipUtils.readTextFileFromZip(stream, this::map);
    }

    private void registerPolicySync(Set<String> policyNames, boolean refresh) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
            @Override
            public void afterCommit() {
                policyNames.forEach(p -> pdlPolicyService.policyUpdated(p, refresh));
            }
        });
    }

    private Policy map(String fileName, String text) {
        String name = StringUtils.removeEnd(fileName, POLICY_FILE_EXTENSION);
        return Opt.of(policyRepository.findByName(name))
                .ifPresent(policy -> policy.setPolicy(text))
                .orElseGet(() -> {
                    Policy policy = new Policy();
                    policy.setName(name);
                    policy.setPolicy(text);
                    return policy;
                });
    }

    private PolicyDto map(Policy policy) {
        PolicyDto dto = new PolicyDto(policy.getId(), policy.getName());
        dto.setPolicy(policy.getPolicy());
        return dto;
    }

    private void map(PolicyDto dto, Policy policy) {
        policy.setName(dto.getName());
        policy.setPolicy(dto.getPolicy());
    }

}
