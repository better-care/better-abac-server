package care.better.abac.rest;

import care.better.abac.AbacConfiguration;
import care.better.abac.content.AppContentSyncConfiguration;
import care.better.abac.dto.DtoWithId;
import care.better.abac.dto.PartyDto;
import care.better.abac.dto.PartyRelationDto;
import care.better.abac.dto.PartyTypeDto;
import care.better.abac.dto.PolicyDto;
import care.better.abac.dto.RelationTypeDto;
import care.better.abac.dto.content.AppContentDto;
import care.better.abac.dto.content.AppContentResultLogLevel;
import care.better.abac.dto.content.AppContentSyncResultDto;
import care.better.abac.dto.content.PlainPartyRelationDto;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

/**
 * @author Matic Ribic
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = AbacConfiguration.class)
@EnableConfigurationProperties
@EnableAutoConfiguration(exclude = {SecurityAutoConfiguration.class, ManagementWebSecurityAutoConfiguration.class})
@AutoConfigureTestDatabase
@TestPropertySource(properties = {
        "content-sync.party-types.included=FORM,ROLE,TEMPLATE,ORGANISATION,PATIENT",
        "content-sync.relation-types.included=HAS_TEMPLATE,CAN_VIEW,CAN_DRAFT,CAN_DELETE,CAN_BE_CREATED"
})
public class AppContentResourceFilteredByEnvConfigTest extends AbstractAppContentResourceTest {

    @Inject
    private AppContentSyncConfiguration appContentSyncConfiguration;

    @Before
    public void assertConfiguration() {
        AppContentSyncConfiguration.PartyTypes partyTypes = appContentSyncConfiguration.getPartyTypes();
        assertThat(partyTypes).isNotNull();
        assertThat(partyTypes.getIncluded()).isNotNull();
        assertThat(partyTypes.getIncluded()).containsExactlyInAnyOrder("FORM", "ROLE", "TEMPLATE", "ORGANISATION", "PATIENT");

        AppContentSyncConfiguration.RelationTypes relationTypes = appContentSyncConfiguration.getRelationTypes();
        assertThat(relationTypes).isNotNull();
        assertThat(relationTypes.getIncluded()).isNotNull();
        assertThat(relationTypes.getIncluded()).containsExactlyInAnyOrder("HAS_TEMPLATE", "CAN_VIEW", "CAN_DRAFT", "CAN_DELETE", "CAN_BE_CREATED");
    }

    @Test
    public void getContent() {
        // given
        JsonNode testDataNode = getTestDataNode("appcontent/testCase-getFilteredContent.json");
        AppContentDto inputContentDto = getAppContent(testDataNode, "init");
        initTestCaseEnvironment(inputContentDto);

        // when
        ResponseEntity<AppContentDto> response = restTemplate.exchange(BASE_URL, HttpMethod.GET, null, AppContentDto.class);

        // then
        AppContentDto expectedContentDto = getAppContent(testDataNode, "content");
        assertAppContent(response, expectedContentDto);
    }

    @Test
    public void submitUpdatedContentFilteredByEnvConfig() {
        // given
        JsonNode testDataNode = getTestDataNode("appcontent/testCase-submitUpdatedContentFilteredByEnvConfig.json");
        AppContentDto initContent = getAppContent(testDataNode, "init");
        initTestCaseEnvironment(initContent);
        AppContentDto updatedContent = getAppContent(testDataNode, "submitInput");

        // when
        ResponseEntity<AppContentSyncResultDto> submitResponse = restTemplate.exchange(BASE_URL + "?resultLogLevel=" + AppContentResultLogLevel.ALL,
                                                                                       HttpMethod.POST,
                                                                                       new HttpEntity<>(updatedContent),
                                                                                       AppContentSyncResultDto.class);
        ResponseEntity<AppContentDto> responseAfterSubmit = restTemplate.exchange(BASE_URL, HttpMethod.GET, null, AppContentDto.class);

        // then
        assertSyncResult(submitResponse, getSyncResult(testDataNode, "resultAll"));
        assertAppContent(responseAfterSubmit, updatedContent);
        assertExcludedEntities(testDataNode, updatedContent);
    }

    @Test
    public void submitUpdatedContentWithExcludedPartyTypeFailed() {
        submitUpdatedContentWithExcludedDataFailed("appcontent/testCase-submitFilteredContentWithExcludedPartyType.json",
                                                   "SYNC-ABAC-001: Unsupported party type USER cannot be synchronized!");
    }

    @Test
    public void submitUpdatedContentWithExcludedPartyFailed() {
        submitUpdatedContentWithExcludedDataFailed("appcontent/testCase-submitFilteredContentWithExcludedParty.json",
                                                   "SYNC-ABAC-010: Party with external ids [Henry King] of unsupported type USER cannot be synchronized!");
    }

    @Test
    public void submitUpdatedContentWithExcludedRelationTypeFailed() {
        submitUpdatedContentWithExcludedDataFailed("appcontent/testCase-submitFilteredContentWithExcludedRelationType.json",
                                                   "SYNC-ABAC-020: Unsupported relation type CAN_CREATE_EXACTLY cannot be synchronized!");
    }

    @Test
    public void submitUpdatedContentWithPartyRelationOfExcludedRelationTypeFailed() {
        submitUpdatedContentWithExcludedDataFailed("appcontent/testCase-submitFilteredContentWithPartyRelationOfExcludedRelationType.json",
                                                   "SYNC-ABAC-030: " +
                                                           "PartyRelation{relationType='CAN_VIEW_EXACTLY', " +
                                                           "source='Party{type='ROLE', externalIds='[CLINICIAN]'}', " +
                                                           "target='Party{type='FORM', externalIds='[About me]'}', " +
                                                           "validUntil='null'} " +
                                                           "of unsupported type CAN_VIEW_EXACTLY cannot be synchronized!");
    }

    @Test
    public void submitUpdatedContentWithPartyRelationOfExcludedSourcePartyTypeFailed() {
        submitUpdatedContentWithExcludedDataFailed("appcontent/testCase-submitFilteredContentWithPartyRelationOfExcludedSourcePartyType.json",
                                                   "SYNC-ABAC-032: " +
                                                           "PartyRelation{relationType='CAN_CREATE', " +
                                                           "source='Party{type='USER', externalIds='[Jane Smith]'}', " +
                                                           "target='Party{type='FORM', externalIds='[About me]'}', " +
                                                           "validUntil='null'} " +
                                                           "with party of unsupported type USER cannot be synchronized!");
    }

    @Test
    public void submitUpdatedContentWithPartyRelationOfExcludedTargetPartyTypeFailed() {
        submitUpdatedContentWithExcludedDataFailed("appcontent/testCase-submitFilteredContentWithPartyRelationOfExcludedTargetPartyType.json",
                                                   "SYNC-ABAC-032: " +
                                                           "PartyRelation{relationType='CAN_BE_VIEWED_BY', " +
                                                           "source='Party{type='FORM', externalIds='[About me]'}', " +
                                                           "target='Party{type='USER', externalIds='[Jane Smith]'}', " +
                                                           "validUntil='null'} " +
                                                           "with party of unsupported type USER cannot be synchronized!");
    }

    private void submitUpdatedContentWithExcludedDataFailed(String testDataResourceName, String expectedErrorMessage) {
        // given
        JsonNode testDataNode = getTestDataNode(testDataResourceName);
        AppContentDto initContent = getAppContent(testDataNode, "init");
        initTestCaseEnvironment(initContent);
        AppContentDto updatedContent = getAppContent(testDataNode, "submitInput");

        // when
        ResponseEntity<ExceptionData> submitResponse = restTemplate.exchange(BASE_URL,
                                                                             HttpMethod.POST,
                                                                             new HttpEntity<>(updatedContent),
                                                                             ExceptionData.class);
        ResponseEntity<AppContentDto> responseAfterSubmit = restTemplate.exchange(BASE_URL, HttpMethod.GET, null, AppContentDto.class);

        // then
        assertThat(submitResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(submitResponse.getBody()).isNotNull();
        assertThat(submitResponse.getBody()).isInstanceOf(ExceptionData.class);
        ExceptionData exceptionData = submitResponse.getBody();
        assertThat(exceptionData.getExceptionName()).isEqualTo("AppContentSyncException");
        assertThat(exceptionData.getErrorMessage()).isEqualTo(expectedErrorMessage);

        AppContentDto contentAfterFailedSubmit = getAppContent(testDataNode, "contentAfterFailedSubmit");
        assertAppContent(responseAfterSubmit, contentAfterFailedSubmit);
        assertExcludedEntities(testDataNode, contentAfterFailedSubmit);
    }

    @SuppressWarnings("AnonymousInnerClassMayBeStatic")
    private void assertExcludedEntities(JsonNode testDataNode, AppContentDto submittedInputContent) {
        AppContentDto contentExcludedFromSync = getAppContent(testDataNode, "excludedFromSync");
        assertThat(contentExcludedFromSync.isEmpty()).isFalse();

        // preconditions
        assertThat(contentExcludedFromSync.getPartyTypes()).doesNotContainAnyElementsOf(submittedInputContent.getPartyTypes());
        assertThat(contentExcludedFromSync.getParties()).doesNotContainAnyElementsOf(submittedInputContent.getParties());
        assertThat(contentExcludedFromSync.getRelationTypes()).doesNotContainAnyElementsOf(submittedInputContent.getRelationTypes());
        assertThat(contentExcludedFromSync.getPartyRelations()).doesNotContainAnyElementsOf(submittedInputContent.getPartyRelations());
        assertThat(contentExcludedFromSync.getPolicies()).doesNotContainAnyElementsOf(submittedInputContent.getPolicies());

        // party types
        contentExcludedFromSync.getPartyTypes().forEach(excludedPartyType -> {
            PartyTypeDto actualPartyType = exchangeGet(PartyTypeResourceTest.BASE_URL + "/name/" + excludedPartyType.getName(), PartyTypeDto.class);
            assertThat(actualPartyType.getName()).isEqualTo(excludedPartyType.getName());
        });

        // parties
        contentExcludedFromSync.getParties().forEach(excludedParty -> {
            String externalId = excludedParty.getExternalIds().iterator().next();
            PartyDto actualParty = exchangeGet(PartyResourceTest.BASE_URL + '/' + excludedParty.getType() + '/' + externalId, PartyDto.class);
            assertThat(actualParty.getType()).isEqualTo(excludedParty.getType());
            assertThat(actualParty.getExternalIds()).containsExactlyInAnyOrderElementsOf(excludedParty.getExternalIds());
        });

        // relation types
        contentExcludedFromSync.getRelationTypes().forEach(excludedRelationType -> {
            RelationTypeDto actualRelationType = exchangeGet(RelationTypeResourceTest.BASE_URL + "/name/" + excludedRelationType.getName(),
                                                             RelationTypeDto.class);
            assertThat(actualRelationType.getName()).isEqualTo(excludedRelationType.getName());
            assertThat(actualRelationType.getAllowedSourcePartyType()).isEqualTo(excludedRelationType.getAllowedSourcePartyType());
            assertThat(actualRelationType.getAllowedTargetPartyType()).isEqualTo(excludedRelationType.getAllowedTargetPartyType());
        });

        // party relations
        List<PlainPartyRelationDto> expectedPartyRelations = contentExcludedFromSync.getPartyRelations();
        if (!expectedPartyRelations.isEmpty()) {
            List<PartyRelationDto> allActualRelations = exchangeGet(PartyRelationResourceTest.BASE_URL,
                                                                    new ParameterizedTypeReference<List<PartyRelationDto>>() {
                                                                    });
            Map<Long, PartyDto> allActualPartiesByIds = exchangeGet(PartyResourceTest.BASE_URL, new ParameterizedTypeReference<List<PartyDto>>() {
            }).stream().collect(Collectors.toMap(DtoWithId::getId, Function.identity()));

            expectedPartyRelations.forEach(excludedRelation -> {
                boolean actualRelationNotFound = allActualRelations.stream()
                        .filter(actualRelation -> actualRelation.getRelationType().equals(excludedRelation.getRelationType()))
                        .filter(actualRelation -> {
                            PartyDto actualSource = allActualPartiesByIds.get(actualRelation.getSource());
                            return actualSource.getType().equals(excludedRelation.getSource().getType()) && actualSource.getExternalIds()
                                    .equals(excludedRelation.getSource().getExternalIds());
                        }).noneMatch(actualRelation -> {
                            PartyDto actualTarget = allActualPartiesByIds.get(actualRelation.getTarget());
                            return actualTarget.getType().equals(excludedRelation.getTarget().getType()) && actualTarget.getExternalIds()
                                    .equals(excludedRelation.getTarget().getExternalIds());
                        });

                if (actualRelationNotFound) {
                    fail("Party relation, excluded from synchronization, not found: " + excludedRelation);
                }
            });
        }

        // policies
        contentExcludedFromSync.getPolicies().forEach(excludedPolicy -> {
            PolicyDto actualPolicy = exchangeGet(PolicyResourceTest.BASE_URL + "/name/" + excludedPolicy.getName(), PolicyDto.class);
            assertThat(actualPolicy.getName()).isEqualTo(excludedPolicy.getName());
            assertThat(actualPolicy.getPolicy()).isEqualTo(excludedPolicy.getPolicy());
        });
    }
}
