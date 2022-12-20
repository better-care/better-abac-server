package care.better.abac.rest;

import care.better.abac.AbacConfiguration;
import care.better.abac.dto.content.AppContentDto;
import care.better.abac.dto.content.AppContentResultLogLevel;
import care.better.abac.dto.content.AppContentSyncResultDto;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Matic Ribic
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = AbacConfiguration.class)
@EnableConfigurationProperties
@EnableAutoConfiguration(exclude = {SecurityAutoConfiguration.class, ManagementWebSecurityAutoConfiguration.class})
@AutoConfigureTestDatabase
public class AppContentResourceTest extends AbstractAppContentResourceTest {

    @Test
    public void getContent() {
        // given
        AppContentDto expectedContentDto = getAppContent(getTestDataNode("appcontent/testCase-getContent.json"), "content");
        initTestCaseEnvironment(expectedContentDto);

        // when
        ResponseEntity<AppContentDto> response = restTemplate.exchange(BASE_URL, HttpMethod.GET, null, AppContentDto.class);

        // then
        assertAppContent(response, expectedContentDto);
    }

    @Test
    public void getEmptyContent() {
        // when
        ResponseEntity<AppContentDto> response = restTemplate.exchange(BASE_URL, HttpMethod.GET, null, AppContentDto.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(response.getBody()).isNull();
    }

    @Test
    public void submitInitialContent() {
        // given
        JsonNode testDataNode = getTestDataNode("appcontent/testCase-submitInitialContent.json");
        AppContentDto inputContent = getAppContent(testDataNode, "input");

        // when
        ResponseEntity<AppContentSyncResultDto> submitResponse = restTemplate.exchange(BASE_URL,
                                                                                       HttpMethod.POST,
                                                                                       new HttpEntity<>(inputContent),
                                                                                       AppContentSyncResultDto.class);
        ResponseEntity<AppContentDto> responseAfterSubmit = restTemplate.exchange(BASE_URL, HttpMethod.GET, null, AppContentDto.class);

        // then
        assertSyncResult(submitResponse, getSyncResult(testDataNode, "result"));
        assertAppContent(responseAfterSubmit, inputContent);
    }

    @Test
    public void submitUpdatedContent() {
        // given
        JsonNode testDataNode = getTestDataNode("appcontent/testCase-submitUpdatedContent.json");
        AppContentDto initContent = getAppContent(testDataNode, "init");
        initTestCaseEnvironment(initContent);
        AppContentDto updatedContent = getAppContent(testDataNode, "submitInput");

        // when
        ResponseEntity<AppContentSyncResultDto> submitResponse = restTemplate.exchange(BASE_URL,
                                                                                       HttpMethod.POST,
                                                                                       new HttpEntity<>(updatedContent),
                                                                                       AppContentSyncResultDto.class);
        ResponseEntity<AppContentDto> responseAfterSubmit = restTemplate.exchange(BASE_URL, HttpMethod.GET, null, AppContentDto.class);

        // then
        assertSyncResult(submitResponse, getSyncResult(testDataNode, "result"));
        assertAppContent(responseAfterSubmit, updatedContent);
    }

    @Test
    public void submitUpdatedContentInDryRunMode() {
        // given
        JsonNode testDataNode = getTestDataNode("appcontent/testCase-submitUpdatedContent.json");
        AppContentDto initContent = getAppContent(testDataNode, "init");
        initTestCaseEnvironment(initContent);
        AppContentDto updatedContent = getAppContent(testDataNode, "submitInput");

        // when
        ResponseEntity<AppContentSyncResultDto> dryRunResponse = restTemplate.exchange(BASE_URL + "?dryRun=true",
                                                                                       HttpMethod.POST,
                                                                                       new HttpEntity<>(updatedContent),
                                                                                       AppContentSyncResultDto.class);
        ResponseEntity<AppContentDto> responseAfterDryRun = restTemplate.exchange(BASE_URL, HttpMethod.GET, null, AppContentDto.class);

        // then
        assertSyncResult(dryRunResponse, getSyncResult(testDataNode, "result"));
        assertAppContent(responseAfterDryRun, initContent);
    }

    @Test
    public void submitAndReturnOnlyLogsForChangedEntities() {
        submitAndReturnOnlyLogsForChangedEntities(BASE_URL + "?resultLogLevel=" + AppContentResultLogLevel.CHANGES_ONLY);
    }

    @Test
    public void submitAndReturnOnlyLogsForChangedEntitiesAsDefaultParameter() {
        submitAndReturnOnlyLogsForChangedEntities(BASE_URL);
    }

    private void submitAndReturnOnlyLogsForChangedEntities(String url) {
        // given
        JsonNode testDataNode = getTestDataNode("appcontent/testCase-submitUpdatedContent.json");
        AppContentDto initContent = getAppContent(testDataNode, "init");
        initTestCaseEnvironment(initContent);
        AppContentDto updatedContent = getAppContent(testDataNode, "submitInput");

        // when
        ResponseEntity<AppContentSyncResultDto> submitResponse = restTemplate.exchange(url,
                                                                                       HttpMethod.POST,
                                                                                       new HttpEntity<>(updatedContent),
                                                                                       AppContentSyncResultDto.class);

        // then
        assertSyncResult(submitResponse, getSyncResult(testDataNode, "result"));
    }

    @Test
    public void submitAndReturnAllLogsForChangedEntities() {
        // given
        JsonNode testDataNode = getTestDataNode("appcontent/testCase-submitUpdatedContent.json");
        AppContentDto initContent = getAppContent(testDataNode, "init");
        initTestCaseEnvironment(initContent);
        AppContentDto updatedContent = getAppContent(testDataNode, "submitInput");

        // when
        ResponseEntity<AppContentSyncResultDto> submitResponse = restTemplate.exchange(BASE_URL + "?resultLogLevel=" + AppContentResultLogLevel.ALL,
                                                                                       HttpMethod.POST,
                                                                                       new HttpEntity<>(updatedContent),
                                                                                       AppContentSyncResultDto.class);

        // then
        assertSyncResult(submitResponse, getSyncResult(testDataNode, "resultAll"));
    }

    @Test
    public void submitAndReturnNoLogs() {
        // given
        JsonNode testDataNode = getTestDataNode("appcontent/testCase-submitUpdatedContent.json");
        AppContentDto initContent = getAppContent(testDataNode, "init");
        initTestCaseEnvironment(initContent);
        AppContentDto updatedContent = getAppContent(testDataNode, "submitInput");

        // when
        ResponseEntity<AppContentSyncResultDto> submitResponse = restTemplate.exchange(BASE_URL + "?resultLogLevel=" + AppContentResultLogLevel.NONE,
                                                                                       HttpMethod.POST,
                                                                                       new HttpEntity<>(updatedContent),
                                                                                       AppContentSyncResultDto.class);

        // then
        assertThat(submitResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(submitResponse.getBody()).isNull();
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

        assertAppContent(responseAfterSubmit, initContent);
    }
}
