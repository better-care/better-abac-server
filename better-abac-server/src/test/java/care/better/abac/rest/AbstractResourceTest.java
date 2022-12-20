package care.better.abac.rest;

import care.better.abac.dto.DtoWithId;
import care.better.abac.dto.PartyDto;
import care.better.abac.dto.PartyRelationDto;
import care.better.abac.dto.PartyTypeDto;
import care.better.abac.dto.PolicyDto;
import care.better.abac.dto.RelationTypeDto;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Matic Ribič
 */
public abstract class AbstractResourceTest {

    @SuppressWarnings("ProtectedField")
    @Autowired
    protected TestRestTemplate restTemplate;

    protected PartyTypeDto createPartyType(PartyTypeDto dto) {
        return createEntity(PartyTypeResourceTest.BASE_URL, dto);
    }

    protected PartyTypeDto createPartyType(String name) {
        return createPartyType(new PartyTypeDto(null, name));
    }

    protected PartyRelationDto createPartyRelation(PartyRelationDto dto) {
        return createEntity(PartyRelationResourceTest.BASE_URL, dto);
    }

    protected PartyDto createParty(PartyDto dto) {
        return createEntity(PartyResourceTest.BASE_URL, dto);
    }

    protected PartyDto createParty(String type, Set<String> externalIds) {
        PartyDto partyDto = new PartyDto(null, type);
        partyDto.setExternalIds(externalIds);
        return createEntity(PartyResourceTest.BASE_URL, partyDto);
    }

    protected PolicyDto createPolicy(PolicyDto dto) {
        return createEntity(PolicyResourceTest.BASE_URL, dto);
    }

    protected PolicyDto createPolicy(String name, String policy) {
        PolicyDto dto = new PolicyDto(null, name);
        dto.setPolicy(policy);
        return createPolicy(dto);
    }

    protected RelationTypeDto createRelationType(RelationTypeDto dto) {
        return createEntity(RelationTypeResourceTest.BASE_URL, dto);
    }

    protected RelationTypeDto createRelationType(String name, String allowedSourcePartyType, String allowedTargetPartyType) {
        RelationTypeDto relationTypeDto = new RelationTypeDto(null, name);
        relationTypeDto.setAllowedSourcePartyType(allowedSourcePartyType);
        relationTypeDto.setAllowedTargetPartyType(allowedTargetPartyType);
        return createEntity(RelationTypeResourceTest.BASE_URL, relationTypeDto);
    }

    @SuppressWarnings("unchecked")
    protected <T extends DtoWithId> T createEntity(String baseUrl, T dto) {
        ResponseEntity<T> createdResponse = restTemplate.postForEntity(baseUrl, dto, (Class<T>)dto.getClass());
        assertThat(createdResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        T body = createdResponse.getBody();
        assertThat(body).describedAs("Empty body").isNotNull();
        Long id = body.getId();
        assertThat(id).describedAs("Missing ID").isNotNull();
        return body;
    }

    protected <T extends DtoWithId> T updateEntity(String baseUrl, long id, T dto) {
        ResponseEntity<T> updatedResponse = exchangePut(baseUrl, id, dto);
        assertThat(updatedResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        T body = updatedResponse.getBody();
        assertThat(body).describedAs("Empty body").isNotNull();
        assertThat(body.getId()).describedAs("Missing ID").isNotNull();
        assertThat(body.getId()).isEqualTo(id);
        return body;
    }

    @SuppressWarnings("unchecked")
    protected <T> ResponseEntity<T> exchangePut(String baseUrl, long id, T dto) {
        return restTemplate.exchange(baseUrl + '/' + id, HttpMethod.PUT, new HttpEntity<>(dto), (Class<T>)dto.getClass());
    }

    protected ResponseEntity<Void> exchangeDelete(String baseUrl, Long id) {
        return restTemplate.exchange(baseUrl + '/' + id, HttpMethod.DELETE, null, Void.class);
    }

    protected <T> T exchangeGet(String url, Class<T> responseType) {
        ResponseEntity<T> response = restTemplate.exchange(url, HttpMethod.GET, null, responseType);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        return response.getBody();
    }

    protected <T> T exchangeGet(String url, ParameterizedTypeReference<T> responseType) {
        ResponseEntity<T> response = restTemplate.exchange(url, HttpMethod.GET, null, responseType);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        return response.getBody();
    }

    @AfterEach
    public void tearDown() {
        for (String baseUrl : Arrays.asList(PartyRelationResourceTest.BASE_URL,
                                            RelationTypeResourceTest.BASE_URL,
                                            PartyResourceTest.BASE_URL,
                                            PartyTypeResourceTest.BASE_URL,
                                            PolicyResourceTest.BASE_URL)) {
            ResponseEntity<List<? extends DtoWithId>> responseEntity = restTemplate.exchange(baseUrl,
                                                                                             HttpMethod.GET,
                                                                                             null,
                                                                                             new DtoListParameterizedTypeReference());
            assertThat(responseEntity.getStatusCode().is2xxSuccessful()).isTrue();
            if (responseEntity.getBody() != null) {
                responseEntity.getBody().forEach(dtoWithId -> {
                    ResponseEntity<Void> deleteResponse = exchangeDelete(baseUrl, dtoWithId.getId());
                    assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
                });
            }
        }
    }

    private static class DtoListParameterizedTypeReference extends ParameterizedTypeReference<List<? extends DtoWithId>> {
    }
}
