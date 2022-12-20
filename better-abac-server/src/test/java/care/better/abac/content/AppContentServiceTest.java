package care.better.abac.content;

import care.better.abac.AbacConfiguration;
import care.better.abac.dto.content.AppContentDto;
import care.better.abac.dto.content.AppContentDtoMapper;
import care.better.abac.dto.content.AppContentResultLogLevel;
import care.better.abac.dto.content.AppContentSyncResultDto;
import care.better.abac.dto.content.AppContentSyncResultDtoMapper;
import care.better.abac.dto.content.PlainPartyDto;
import care.better.abac.dto.content.PlainPartyRelationDto;
import care.better.abac.dto.content.PlainPartyTypeDto;
import care.better.abac.dto.content.PlainPolicyDto;
import care.better.abac.dto.content.PlainRelationTypeDto;
import care.better.abac.exception.AppContentSyncException;
import care.better.abac.init.InitTestEnvConfiguration;
import care.better.abac.init.InitTestEnvService;
import care.better.abac.jpa.entity.Party;
import care.better.abac.jpa.entity.PartyRelation;
import care.better.abac.jpa.entity.RelationType;
import care.better.abac.rest.AppContentResourceTest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.AbstractThrowableAssert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * @author Matic Ribic
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = {AbacConfiguration.class, InitTestEnvConfiguration.class})
@EnableConfigurationProperties
@EnableAutoConfiguration(exclude = {SecurityAutoConfiguration.class, ManagementWebSecurityAutoConfiguration.class})
@AutoConfigureTestDatabase
public class AppContentServiceTest {

    @Inject
    private AppContentService appContentService;

    @Inject
    private InitTestEnvService initTestEnvService;

    @Inject
    private ObjectMapper objectMapper;

    @Test
    public void getContent() {
        // given
        AppContent expectedContent = getAppContent(getTestDataNode("appcontent/testCase-getContent.json"), "content");
        initTestCaseEnvironment(expectedContent);

        // when
        AppContent appContent = appContentService.getContent(getDefaultRequestContext());

        // then
        assertAppContent(appContent, expectedContent);
    }

    @Test
    public void getEmptyContent() {
        // when
        AppContent appContent = appContentService.getContent(getDefaultRequestContext());

        // then
        assertThat(appContent).isNotNull();
        assertThat(appContent.isEmpty()).isTrue();
    }

    @Test
    public void submitInitialContent() {
        // given
        JsonNode testDataNode = getTestDataNode("appcontent/testCase-submitInitialContent.json");
        AppContent inputContent = getAppContent(testDataNode, "input");

        // when
        AppContentSyncResult actualSyncResult = appContentService.submitContent(inputContent,
                                                                                getUnfilteredRequestContext(false, AppContentResultLogLevel.CHANGES_ONLY));
        AppContent contentAfterSubmit = appContentService.getContent(getDefaultRequestContext());

        // then
        assertSyncResult(actualSyncResult, getSyncResult(testDataNode, "result"));
        assertAppContent(contentAfterSubmit, inputContent);
    }

    @Test
    public void submitUpdatedContent() {
        // given
        JsonNode testDataNode = getTestDataNode("appcontent/testCase-submitUpdatedContent.json");
        AppContent initContent = getAppContent(testDataNode, "init");
        initTestCaseEnvironment(initContent);
        AppContent updatedContent = getAppContent(testDataNode, "submitInput");

        // when
        AppContentSyncResult actualSyncResult = appContentService.submitContent(updatedContent,
                                                                                getUnfilteredRequestContext(false, AppContentResultLogLevel.CHANGES_ONLY));
        AppContent contentAfterSubmit = appContentService.getContent(getDefaultRequestContext());

        // then
        assertSyncResult(actualSyncResult, getSyncResult(testDataNode, "result"));
        assertAppContent(contentAfterSubmit, updatedContent);
    }

    @Test
    public void submitUpdatedContentInDryRunMode() {
        // given
        JsonNode testDataNode = getTestDataNode("appcontent/testCase-submitUpdatedContent.json");
        AppContent initContent = getAppContent(testDataNode, "init");
        initTestCaseEnvironment(initContent);
        AppContent updatedContent = getAppContent(testDataNode, "submitInput");

        // when
        AppContentSyncResult dryRunResult = appContentService.submitContent(updatedContent,
                                                                            getUnfilteredRequestContext(true, AppContentResultLogLevel.CHANGES_ONLY));
        AppContent afterDryRunContent = appContentService.getContent(getDefaultRequestContext());

        // then
        assertSyncResult(dryRunResult, getSyncResult(testDataNode, "result"));
        assertAppContent(afterDryRunContent, initContent);
    }

    @Test
    public void submitAndReturnOnlyLogsForChangedEntities() {
        // given
        JsonNode testDataNode = getTestDataNode("appcontent/testCase-submitUpdatedContent.json");
        AppContent initContent = getAppContent(testDataNode, "init");
        initTestCaseEnvironment(initContent);
        AppContent updatedContent = getAppContent(testDataNode, "submitInput");

        // when
        AppContentSyncResult actualSyncResult = appContentService.submitContent(updatedContent,
                                                                                getUnfilteredRequestContext(false, AppContentResultLogLevel.CHANGES_ONLY));

        // then
        assertSyncResult(actualSyncResult, getSyncResult(testDataNode, "result"));
    }

    @Test
    public void submitAndReturnAllLogsForChangedEntities() {
        // given
        JsonNode testDataNode = getTestDataNode("appcontent/testCase-submitUpdatedContent.json");
        AppContent initContent = getAppContent(testDataNode, "init");
        initTestCaseEnvironment(initContent);
        AppContent updatedContent = getAppContent(testDataNode, "submitInput");

        // when
        AppContentSyncResult actualSyncResult = appContentService.submitContent(updatedContent,
                                                                                getUnfilteredRequestContext(false, AppContentResultLogLevel.ALL));

        // then
        assertSyncResult(actualSyncResult, getSyncResult(testDataNode, "resultAll"));
    }

    @Test
    public void submitAndReturnNoLogs() {
        // given
        JsonNode testDataNode = getTestDataNode("appcontent/testCase-submitUpdatedContent.json");
        AppContent initContent = getAppContent(testDataNode, "init");
        initTestCaseEnvironment(initContent);
        AppContent updatedContent = getAppContent(testDataNode, "submitInput");

        // when
        AppContentSyncResult actualSyncResult = appContentService.submitContent(updatedContent,
                                                                                getUnfilteredRequestContext(false, AppContentResultLogLevel.NONE));

        // then
        assertThat(actualSyncResult).isNotNull();
        assertThat(actualSyncResult.isEmpty()).isTrue();
    }

    @Test
    public void submitPartiesWithMultipleExternalIds() {
        // given
        JsonNode testDataNode = getTestDataNode("appcontent/testCase-submitPartiesWithMultipleExternalIds.json");
        AppContent initContent = getAppContent(testDataNode, "init");
        initTestCaseEnvironment(initContent);
        AppContent updatedContent = getAppContent(testDataNode, "submitInput");

        // when
        AppContentSyncResult actualSyncResult = appContentService.submitContent(updatedContent,
                                                                                getUnfilteredRequestContext(false, AppContentResultLogLevel.ALL));
        AppContent contentAfterSubmit = appContentService.getContent(getDefaultRequestContext());

        // then
        assertSyncResult(actualSyncResult, getSyncResult(testDataNode, "resultAll"));

        assertAppContent(contentAfterSubmit, updatedContent);
    }

    @Test
    public void submitPartyRelationWithInvalidExternalIdsFailed() {
        submitContentFailed("appcontent/testCase-submitPartyRelationWithInvalidExternalIds.json",
                            "SYNC-ABAC-033: " +
                                    "PartyRelation{relationType='IS_MANAGER_OF', " +
                                    "source='Party{type='USER', externalIds='[Jane Smith]'}', " +
                                    "target='Party{type='CARE_HOME', externalIds='[Brook House]'}', " +
                                    "validUntil='null'} " +
                                    "with missing party Party{type='CARE_HOME', externalIds='[Brook House]'} cannot be synchronized!");
    }

    @Test
    public void submitContentWithMissingPartyTypeFailed() {
        submitContentFailed("appcontent/testCase-submitContentWithMissingPartyType.json",
                            "SYNC-ABAC-011: Party with external ids [Personal information] of missing type FORM cannot be synchronized!");
    }

    @Test
    public void submitContentWithPartyRelationOfMissingRelationTypeFailed() {
        submitContentFailed("appcontent/testCase-submitContentWithPartyRelationOfMissingRelationType.json",
                            "SYNC-ABAC-031: " +
                                    "PartyRelation{relationType='CAN_DELETE', " +
                                    "source='Party{type='ROLE', externalIds='[CLINICIAN]'}', " +
                                    "target='Party{type='FORM', externalIds='[Personal information]'}', " +
                                    "validUntil='null'} " +
                                    "of missing type CAN_DELETE cannot be synchronized!");
    }

    @Test
    public void submitContentWithPartyRelationWithMissingSourcePartyFailed() {
        submitContentFailed("appcontent/testCase-submitContentWithPartyRelationWithMissingSourceParty.json",
                            "SYNC-ABAC-033: " +
                                    "PartyRelation{relationType='CAN_VIEW', " +
                                    "source='Party{type='ROLE', " +
                                    "externalIds='[URGENT_CARE]'}', " +
                                    "target='Party{type='FORM', externalIds='[Personal information]'}', " +
                                    "validUntil='null'} " +
                                    "with missing party Party{type='ROLE', externalIds='[URGENT_CARE]'} cannot be synchronized!");
    }

    @Test
    public void submitContentWithPartyRelationWithMissingTargetPartyFailed() {
        submitContentFailed("appcontent/testCase-submitContentWithPartyRelationWithMissingTargetParty.json",
                            "SYNC-ABAC-033: " +
                                    "PartyRelation{relationType='CAN_VIEW', " +
                                    "source='Party{type='ROLE', externalIds='[URGENT_CARE]'}', " +
                                    "target='Party{type='FORM', externalIds='[Personal information]'}', " +
                                    "validUntil='null'} " +
                                    "with missing party Party{type='FORM', externalIds='[Personal information]'} cannot be synchronized!");
    }

    private void submitContentFailed(String testDataResourceName, String expectedErrorMessage) {
        // given
        JsonNode testDataNode = getTestDataNode(testDataResourceName);
        AppContent initContent = getAppContent(testDataNode, "init");
        initTestCaseEnvironment(initContent);
        AppContent updatedContent = getAppContent(testDataNode, "submitInput");

        // when
        AbstractThrowableAssert<?, ? extends Throwable> throwableAssert =
                assertThatThrownBy(() -> appContentService.submitContent(updatedContent, getUnfilteredRequestContext(false, AppContentResultLogLevel.ALL)));
        AppContent contentAfterSubmit = appContentService.getContent(getDefaultRequestContext());

        // then
        throwableAssert.isInstanceOf(AppContentSyncException.class);
        throwableAssert.hasMessage(expectedErrorMessage);

        assertAppContent(contentAfterSubmit, initContent);
    }

    private void assertAppContent(AppContent actualContent, AppContent expectedContent) {
        assertThat(actualContent).isNotNull();

        assertThat(actualContent.getDtos(PlainPartyTypeDto.class)).containsExactlyInAnyOrderElementsOf(expectedContent.getDtos(PlainPartyTypeDto.class));
        assertThat(actualContent.getDtos(PlainPartyDto.class)).containsExactlyInAnyOrderElementsOf(expectedContent.getDtos(PlainPartyDto.class));
        assertThat(actualContent.getDtos(PlainRelationTypeDto.class)).containsExactlyInAnyOrderElementsOf(expectedContent.getDtos(PlainRelationTypeDto.class));
        assertThat(actualContent.getDtos(PlainPartyRelationDto.class)).containsExactlyInAnyOrderElementsOf(expectedContent.getDtos(PlainPartyRelationDto.class));
        assertThat(actualContent.getDtos(PlainPolicyDto.class)).containsExactlyInAnyOrderElementsOf(expectedContent.getDtos(PlainPolicyDto.class));
    }

    private void assertSyncResult(AppContentSyncResult actualSyncResult, AppContentSyncResult expectedResult) {
        assertThat(actualSyncResult).isNotNull();

        assertThat(actualSyncResult.getResults(PlainPartyTypeDto.class)).containsExactlyInAnyOrderElementsOf(expectedResult.getResults(PlainPartyTypeDto.class));
        assertThat(actualSyncResult.getResults(PlainPartyDto.class)).containsExactlyInAnyOrderElementsOf(expectedResult.getResults(PlainPartyDto.class));
        assertThat(actualSyncResult.getResults(PlainRelationTypeDto.class)).containsExactlyInAnyOrderElementsOf(expectedResult.getResults(PlainRelationTypeDto.class));
        assertThat(actualSyncResult.getResults(PlainPartyRelationDto.class)).containsExactlyInAnyOrderElementsOf(expectedResult.getResults(PlainPartyRelationDto.class));
        assertThat(actualSyncResult.getResults(PlainPolicyDto.class)).containsExactlyInAnyOrderElementsOf(expectedResult.getResults(PlainPolicyDto.class));
    }

    @AfterEach
    public void tearDown() {
        initTestEnvService.deleteAll();
    }

    private void initTestCaseEnvironment(AppContent inputContent) {
        inputContent.getDtos(PlainPartyTypeDto.class).forEach(partyType -> initTestEnvService.createPartyType(partyType.getName()));

        Map<PlainPartyDto, Party> partiesByPlains = inputContent.getDtos(PlainPartyDto.class)
                .stream()
                .map(party -> initTestEnvService.createParty(party.getType(), party.getExternalIds()))
                .collect(Collectors.toMap(party -> {
                    PlainPartyDto plainPartyDto = new PlainPartyDto(party.getType().getName());
                    plainPartyDto.setExternalIds(party.getExternalIds());
                    return plainPartyDto;
                }, Function.identity()));

        Map<String, RelationType> relationTypeByNames = inputContent.getDtos(PlainRelationTypeDto.class)
                .stream()
                .map(relationType -> initTestEnvService.createRelationType(relationType.getName(),
                                                                           relationType.getAllowedSourcePartyType(),
                                                                           relationType.getAllowedTargetPartyType()))
                .collect(Collectors.toMap(RelationType::getName, Function.identity()));


        inputContent.getDtos(PlainPartyRelationDto.class).forEach(partyRelationDto -> {
            PartyRelation partyRelation = new PartyRelation(partiesByPlains.get(partyRelationDto.getSource()),
                                                            partiesByPlains.get(partyRelationDto.getTarget()),
                                                            relationTypeByNames.get(partyRelationDto.getRelationType()));
            partyRelation.setValidUntil(partyRelationDto.getValidUntil());
            initTestEnvService.createPartyRelation(partyRelation);
        });

        inputContent.getDtos(PlainPolicyDto.class).forEach(policy -> initTestEnvService.createPolicy(policy.getName(), policy.getPolicy()));
    }

    private JsonNode getTestDataNode(String resourceName) {
        try (InputStream resource = AppContentResourceTest.class.getClassLoader().getResourceAsStream(resourceName)) {
            return objectMapper.readTree(resource);
        } catch (IOException e) {
//            Assert.ass("Failed to load resource " + resourceName + " with error: " + e.getMessage());
            throw new IllegalStateException(e);
        }
    }

    private AppContent getAppContent(JsonNode node, String field) {
        AppContent content = AppContentDtoMapper.toModel(getField(node, field, AppContentDto.class));
        assertThat(content.isEmpty()).isFalse();
        return content;
    }

    private AppContentSyncResult getSyncResult(JsonNode testDataNode, String result) {
        AppContentSyncResult syncResult = AppContentSyncResultDtoMapper.toModel(getField(testDataNode, result, AppContentSyncResultDto.class));
        assertThat(syncResult.isEmpty()).isFalse();
        return syncResult;
    }

    private <T> T getField(JsonNode node, String field, Class<T> fieldType) {
        try {
            return objectMapper.treeToValue(node.get(field), fieldType);
        } catch (JsonProcessingException e) {
            fail("Failed to parse field " + field + " with error: " + e.getMessage());
            throw new IllegalStateException(e);
        }
    }

    private AppContentRequestContext getDefaultRequestContext() {
        return new AppContentRequestContext(Collections.emptyList(), Collections.emptyList(), false, null);
    }

    private AppContentRequestContext getUnfilteredRequestContext(boolean dryRun, AppContentResultLogLevel resultLogLevel) {
        return new AppContentRequestContext(Collections.emptyList(), Collections.emptyList(), dryRun, resultLogLevel);
    }
}
