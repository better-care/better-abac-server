package care.better.abac.init;

import care.better.abac.KeycloakTest;
import care.better.abac.dto.PartyDto;
import care.better.abac.dto.PartyRelationDto;
import care.better.abac.dto.PartyTypeDto;
import care.better.abac.dto.PolicyDto;
import care.better.abac.dto.RelationTypeDto;
import care.better.abac.jpa.entity.PartyType;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class AbacInitialiserTest extends KeycloakTest {
//    private static final String BASE_URL = "http://localhost:8080/rest/v1";
//    private static final String THINK_EHR_URL = "http://localhost:8081/rest/v1";
    private static final String BASE_URL = "http://localhost:8080/rest/v1/admin";
    private static final String THINK_EHR_URL = "http://thinkehr4.marand.si:38081/rest/v1";
    private static final String THINK_SUBJECT_NAMESPACE = "abac";

    private static final String CLIENT_ID = "thinkabac";
    private static final String USERNAME = "abacadmin";
    private static final String PASSWORD = "1admin3";

    private static final String[] PARTY_TYPES = {"USER", "PATIENT", "ORGANIZATION"};

    private final RestTemplate restTemplate = new RestTemplate();

    @Test
    @Disabled
    public void initDb() throws IOException {
        createPartyTypes();
        createRelationTypes();
        //deleteAllParties();
        //createSampleParties();
        loadPolicy("READ", "/policies/read.pdl");
        loadPolicy("WRITE", "/policies/write.pdl");
    }

    @Override
    protected String getPassword() {
        return PASSWORD;
    }

    @Override
    protected String getUsername() {
        return USERNAME;
    }

    @Override
    protected String getClientId() {
        return CLIENT_ID;
    }

    private void loadPolicy(final String policyName, String policyResource) throws IOException {
        ResponseEntity<PolicyDto> responseEntity = restTemplate.exchange(BASE_URL + "/policy/name/{policyName}", HttpMethod.GET, getRequestEntity(),
                                                                         PolicyDto.class,
                                                                         policyName);
        PolicyDto dto = responseEntity.getBody();
        if (dto == null) {
            dto = new PolicyDto();
        }
        dto.setName(policyName);
        try (BufferedReader buffer = new BufferedReader(new InputStreamReader(AbacInitialiserTest.class.getResourceAsStream(policyResource)))) {
            dto.setPolicy(buffer.lines().collect(Collectors.joining("\n")));
        }
        if (dto.getId() != null) {
            restTemplate.put(BASE_URL + "/policy/{id}", getRequestEntity(dto), dto.getId());
        } else {
            restTemplate.postForLocation(BASE_URL + "/policy", getRequestEntity(dto));
        }
    }

    private void createSampleParties() {
        // users
        PartyDto user1 = createUser("USER", Collections.singleton("john"));
        PartyDto user2 = createUser("USER", Collections.singleton("amy"));
        createUser("USER", Collections.singleton("alison"));
        createUser("USER", Collections.singleton("mary"));

        // patients
        Set<String> ids1 = createPartyAndEhr("Craig", "Brown", "MALE", LocalDate.of(1971, 7, 11));
        Set<String> ids2 = createPartyAndEhr("Denise", "Davies", "FEMALE", LocalDate.of(1954, 3, 17));
        Set<String> ids3 = createPartyAndEhr("Linda", "Evans", "FEMALE", LocalDate.of(2006, 9, 28));
        Set<String> ids4 = createPartyAndEhr("Maria", "Wilson", "FEMALE", LocalDate.of(1986, 2, 18));

        PartyDto patient1 = createUser("PATIENT", ids1); // craig
        PartyDto patient2 = createUser("PATIENT", ids2); // denise
        createUser("PATIENT", ids3); // linda
        createUser("PATIENT", ids4); // maria

        PartyDto organization = createUser("ORGANIZATION", Collections.singleton("pek::psych"));

        // add some relations
        PartyRelationDto rel1 = new PartyRelationDto(null, user1.getId(), "PERSONAL_PHYSICIAN", patient1.getId());
        restTemplate.postForLocation(BASE_URL + "/partyRelation", getRequestEntity(rel1));

        PartyRelationDto rel2 = new PartyRelationDto(null, user2.getId(), "MEMBER_OF", organization.getId());
        restTemplate.postForLocation(BASE_URL + "/partyRelation", getRequestEntity(rel2));

        PartyRelationDto rel3 = new PartyRelationDto(null, patient2.getId(), "ON_WARD", organization.getId());
        restTemplate.postForLocation(BASE_URL + "/partyRelation", getRequestEntity(rel3));
    }

    private Set<String> createPartyAndEhr(String firstName, String lastName, String gender, LocalDate dob) {
        ResponseEntity<JsonNode> responseEntity = restTemplate.exchange(
                UriComponentsBuilder.fromHttpUrl(THINK_EHR_URL + "/demographics/party/query")
                        .queryParam("firstNames", firstName)
                        .queryParam("lastNames", lastName)
                        .build().toUri(),
                HttpMethod.GET, getRequestEntity(), JsonNode.class);
        String id = null;
        String ehrId = null;
        if (responseEntity.getStatusCode() != HttpStatus.NO_CONTENT) {
            for (JsonNode jsonNode : responseEntity.getBody().path("parties")) {
                id = jsonNode.path("id").textValue();
                for (JsonNode additionalInfo : jsonNode.path("partyAdditionalInfo")) {
                    if ("ehrId".equals(additionalInfo.path("key").textValue())) {
                        ehrId = additionalInfo.path("value").textValue();
                    }
                }
                if (id != null && ehrId != null) {
                    break;
                }
            }
        }

        if (id == null) {
            id = createParty(firstName, lastName, gender, dob);
            ehrId = createEhr(id);
            updateAdditionalInfo(firstName, lastName, gender, dob, id, ehrId);
        } else {
            if (ehrId == null) {
                ehrId = findEhr(id);
                if (ehrId == null) {
                    ehrId = createEhr(id);
                }
                updateAdditionalInfo(firstName, lastName, gender, dob, id, ehrId);
            }
        }


        return ImmutableSet.of(id, ehrId);
    }

    private void updateAdditionalInfo(String firstName, String lastName, String gender, LocalDate dob, String id, String ehrId) {

        Map<String, Object> partyData = ImmutableMap.<String, Object>builder()
                .put("id", id)
                .put("firstNames", firstName)
                .put("lastNames", lastName)
                .put("gender", gender)
                .put("dateOfBirth", DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(dob.atTime(LocalTime.MIDNIGHT).atOffset(ZoneOffset.UTC)))
                .put("partyAdditionalInfo", ImmutableList.of(ImmutableMap.of("key", "ehrId", "value", ehrId)))
                .build();
        restTemplate.exchange(THINK_EHR_URL + "/demographics/party", HttpMethod.PUT, getRequestEntity(partyData), JsonNode.class);
    }

    private String createEhr(String id) {
        ResponseEntity<JsonNode> ehrCreateResponse = restTemplate.exchange(
                UriComponentsBuilder.fromHttpUrl(THINK_EHR_URL + "/ehr")
                        .queryParam("subjectId", id)
                        .queryParam("subjectNamespace", THINK_SUBJECT_NAMESPACE)
                        .build().toUri(),
                HttpMethod.POST, getRequestEntity(), JsonNode.class);
        return ehrCreateResponse.getBody().path("ehrId").textValue();
    }

    private String findEhr(String id) {
        try {
            ResponseEntity<JsonNode> ehrCreateResponse = restTemplate.exchange(
                    UriComponentsBuilder.fromHttpUrl(THINK_EHR_URL + "/ehr")
                            .queryParam("subjectId", id)
                            .queryParam("subjectNamespace", THINK_SUBJECT_NAMESPACE)
                            .build().toUri(),
                    HttpMethod.GET, getRequestEntity(), JsonNode.class);
            return ehrCreateResponse.getBody().path("ehrId").textValue();
        } catch (RestClientException ignored) {
            return null;
        }
    }

    private String createParty(String firstName, String lastName, String gender, LocalDate dob) {
        Map<String, String> partyData = ImmutableMap.of(
                "firstNames", firstName,
                "lastNames", lastName,
                "gender", gender,
                "dateOfBirth", DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(dob.atTime(LocalTime.MIDNIGHT).atOffset(ZoneOffset.UTC))
        );
        ResponseEntity<JsonNode> partyCreateResponse = restTemplate.exchange(THINK_EHR_URL + "/demographics/party", HttpMethod.POST,
                                                                             getRequestEntity(partyData),
                                                                             JsonNode.class);
        return getId(partyCreateResponse.getBody().path("meta").path("href").textValue());
    }

    private PartyDto createUser(String user, Set<String> externalIds) {
        PartyDto partyDto = new PartyDto();
        partyDto.setType(user);
        partyDto.setExternalIds(externalIds);
        partyDto = restTemplate.postForObject(BASE_URL + "/party", getRequestEntity(partyDto), PartyDto.class);
        return partyDto;
    }

    public Map<String, Long> createPartyTypes() {
        Map<String, Long> partyTypes = new HashMap<>();
        for (String partyTypeName : PARTY_TYPES) {
            Long partyTypeId = null;
            try {
                ResponseEntity<PartyTypeDto> partyTypeResponse = restTemplate.exchange(BASE_URL + "/partyType/name/{name}", HttpMethod.GET, getRequestEntity(),
                                                                                       PartyTypeDto.class, partyTypeName);
                if (partyTypeResponse.getBody() != null) {
                    partyTypeId = partyTypeResponse.getBody().getId();
                }
            } catch (RestClientException ignored) {
            }

            if (partyTypeId == null) {
                PartyTypeDto dto = new PartyTypeDto();
                dto.setName(partyTypeName);
                partyTypeId = Long.valueOf(getId(restTemplate.postForLocation(BASE_URL + "/partyType", getRequestEntity(dto), PartyType.class)));
            }
            partyTypes.put(partyTypeName, partyTypeId);
        }

        return partyTypes;
    }

    public Map<String, Long> createRelationTypes() {
        Map<String, String> sourcePartyTypes = new HashMap<>();
        sourcePartyTypes.put("PERSONAL_PHYSICIAN", "USER");
        sourcePartyTypes.put("PERSON_OF_TRUST", "USER");
        sourcePartyTypes.put("MEMBER_OF", "USER");
        sourcePartyTypes.put("ON_WARD", "PATIENT");
        sourcePartyTypes.put("ON_DUTY", "USER");
        sourcePartyTypes.put("OWNS", "USER");
        Map<String, String> targetPartyTypes = new HashMap<>();
        targetPartyTypes.put("PERSONAL_PHYSICIAN", "PATIENT");
        targetPartyTypes.put("PERSON_OF_TRUST", "PATIENT");
        targetPartyTypes.put("MEMBER_OF", "ORGANIZATION");
        targetPartyTypes.put("ON_WARD", "ORGANIZATION");
        targetPartyTypes.put("ON_DUTY", "ORGANIZATION");
        targetPartyTypes.put("OWNS", "PATIENT");

        Map<String, Long> relationTypes = new HashMap<>();
        for (String relationTypeName : sourcePartyTypes.keySet()) {
            Long relationTypeId = null;
            try {
                ResponseEntity<RelationTypeDto> relationTypeResponse = restTemplate.exchange(BASE_URL + "/relationType/name/{name}", HttpMethod.GET,
                                                                                             getRequestEntity(), RelationTypeDto.class, relationTypeName);
                if (relationTypeResponse.getBody() != null) {
                    relationTypeId = relationTypeResponse.getBody().getId();
                }
            } catch (RestClientException ignored) {
            }
            if (relationTypeId == null) {
                RelationTypeDto dto = new RelationTypeDto();
                dto.setName(relationTypeName);
                dto.setAllowedSourcePartyType(sourcePartyTypes.get(relationTypeName));
                dto.setAllowedTargetPartyType(targetPartyTypes.get(relationTypeName));
                relationTypeId = Long.valueOf(getId(restTemplate.postForLocation(BASE_URL + "/relationType", getRequestEntity(dto))));
            }
            relationTypes.put(relationTypeName, relationTypeId);
        }

        return relationTypes;
    }

    public void deleteAllParties() {
        ResponseEntity<PartyDto[]> responseEntity = restTemplate.exchange(BASE_URL + "/party", HttpMethod.GET, getRequestEntity(), PartyDto[].class);
        PartyDto[] hits = responseEntity.getBody();

        for (PartyDto dto : hits) {
            restTemplate.exchange(BASE_URL + "/party/{id}", HttpMethod.DELETE, getRequestEntity(), String.class, dto.getId());
        }
    }
}
