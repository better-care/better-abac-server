package care.better.abac.rest;

import care.better.abac.dto.PartyDto;
import care.better.abac.dto.PartyRelationDto;
import care.better.abac.dto.content.AppContentDto;
import care.better.abac.dto.content.AppContentSyncResultDto;
import care.better.abac.dto.content.PlainPartyDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * @author Matic Ribic
 */
public class AbstractAppContentResourceTest extends AbstractResourceTest {
    static final String BASE_URL = "/rest/v1/admin/content";

    @Inject
    private ObjectMapper objectMapper;

    protected void initTestCaseEnvironment(AppContentDto inputContentDto) {
        inputContentDto.getPartyTypes().forEach(partyType -> createPartyType(partyType.getName()));

        Map<PlainPartyDto, PartyDto> partiesByPlainDtos = inputContentDto.getParties()
                .stream()
                .map(party -> createParty(party.getType(), party.getExternalIds()))
                .collect(Collectors.toMap(party -> {
                    PlainPartyDto plainPartyDto = new PlainPartyDto(party.getType());
                    plainPartyDto.setExternalIds(party.getExternalIds());
                    return plainPartyDto;
                }, Function.identity()));

        inputContentDto.getRelationTypes().forEach(relationType -> createRelationType(relationType.getName(),
                                                                                      relationType.getAllowedSourcePartyType(),
                                                                                      relationType.getAllowedTargetPartyType()));


        inputContentDto.getPartyRelations().forEach(partyRelation -> {
            PartyRelationDto dto = new PartyRelationDto(null,
                                                        partiesByPlainDtos.get(partyRelation.getSource()).getId(),
                                                        partyRelation.getRelationType(),
                                                        partiesByPlainDtos.get(partyRelation.getTarget()).getId());
            dto.setValidUntil(partyRelation.getValidUntil());
            createPartyRelation(dto);
        });

        inputContentDto.getPolicies().forEach(policy -> createPolicy(policy.getName(), policy.getPolicy()));
    }

    protected JsonNode getTestDataNode(String resourceName) {
        try (InputStream resource = AbstractAppContentResourceTest.class.getClassLoader().getResourceAsStream(resourceName)) {
            return objectMapper.readTree(resource);
        } catch (IOException e) {
            fail("Failed to load resource " + resourceName + " with error: " + e.getMessage());
            throw new IllegalStateException(e);
        }
    }

    protected AppContentDto getAppContent(JsonNode node, String field) {
        AppContentDto contentDto = getField(node, field, AppContentDto.class);
        assertThat(contentDto.isEmpty()).isFalse();
        return contentDto;
    }

    protected AppContentSyncResultDto getSyncResult(JsonNode node, String field) {
        return getField(node, field, AppContentSyncResultDto.class);
    }

    private  <T> T getField(JsonNode node, String field, Class<T> fieldType) {
        try {
            JsonNode fieldNode = node.get(field);
            if (fieldNode.isMissingNode()) {
                fail("Missing field " + field);
            }

            T value = objectMapper.treeToValue(fieldNode, fieldType);
            assertThat(value).isNotNull();
            return value;
        } catch (JsonProcessingException e) {
            fail("Failed to parse field " + field + " with error: " + e.getMessage());
            throw new IllegalStateException(e);
        }
    }

    protected void assertAppContent(ResponseEntity<AppContentDto> actualResponse, AppContentDto expectedContent) {
        assertThat(actualResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        AppContentDto actualContent = actualResponse.getBody();
        assertThat(actualContent).isNotNull();

        assertThat(actualContent.getPartyTypes()).containsExactlyInAnyOrderElementsOf(expectedContent.getPartyTypes());
        assertThat(actualContent.getParties()).containsExactlyInAnyOrderElementsOf(expectedContent.getParties());
        assertThat(actualContent.getRelationTypes()).containsExactlyInAnyOrderElementsOf(expectedContent.getRelationTypes());
        assertThat(actualContent.getPartyRelations()).containsExactlyInAnyOrderElementsOf(expectedContent.getPartyRelations());
        assertThat(actualContent.getPolicies()).containsExactlyInAnyOrderElementsOf(expectedContent.getPolicies());
    }

    protected void assertSyncResult(ResponseEntity<AppContentSyncResultDto> actualResponse, AppContentSyncResultDto expectedSyncResult) {
        assertThat(actualResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        AppContentSyncResultDto actualSyncResult = actualResponse.getBody();
        assertThat(actualSyncResult).isNotNull();

        assertThat(actualSyncResult.getPartyTypes()).containsExactlyInAnyOrderElementsOf(expectedSyncResult.getPartyTypes());
        assertThat(actualSyncResult.getParties()).containsExactlyInAnyOrderElementsOf(expectedSyncResult.getParties());
        assertThat(actualSyncResult.getRelationTypes()).containsExactlyInAnyOrderElementsOf(expectedSyncResult.getRelationTypes());
        assertThat(actualSyncResult.getPartyRelations()).containsExactlyInAnyOrderElementsOf(expectedSyncResult.getPartyRelations());
        assertThat(actualSyncResult.getPolicies()).containsExactlyInAnyOrderElementsOf(expectedSyncResult.getPolicies());
    }
}
