package care.better.abac.rest;

import care.better.abac.content.AppContent;
import care.better.abac.content.AppContentRequestContext;
import care.better.abac.content.AppContentService;
import care.better.abac.content.AppContentSyncConfiguration;
import care.better.abac.content.AppContentSyncResult;
import care.better.abac.dto.content.AppContentDto;
import care.better.abac.dto.content.AppContentDtoMapper;
import care.better.abac.dto.content.AppContentResultLogLevel;
import care.better.abac.dto.content.AppContentSyncResultDto;
import care.better.abac.dto.content.AppContentSyncResultDtoMapper;
import care.better.abac.exception.AppContentSyncException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Matic Ribic
 */
@Component
@RestController
@RequestMapping("/rest/v1/admin/content")
public class AppContentResource {
    private final AppContentService appContentService;
    private final AppContentSyncConfiguration configuration;

    @Autowired
    public AppContentResource(AppContentService appContentService, AppContentSyncConfiguration configuration) {
        this.appContentService = appContentService;
        this.configuration = configuration;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AppContentDto> getContent() {
        AppContent content = appContentService.getContent(getRequestContext(false, null));
        return content.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(AppContentDtoMapper.toDto(content));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AppContentSyncResultDto> submitConfig(
            @RequestParam(value = "dryRun", required = false, defaultValue = "false") boolean dryRun,
            @RequestParam(value = "resultLogLevel", required = false, defaultValue = "CHANGES_ONLY") AppContentResultLogLevel resultLogLevel,
            @RequestBody(required = false) AppContentDto contentDto) {
        if (contentDto == null || contentDto.isEmpty()) {
            throw AppContentSyncException.EMPTY_BODY;
        }

        AppContentSyncResult syncResult = appContentService.submitContent(AppContentDtoMapper.toModel(contentDto), getRequestContext(dryRun, resultLogLevel));
        return resultLogLevel == AppContentResultLogLevel.NONE
                ? ResponseEntity.ok().build()
                : ResponseEntity.ok(AppContentSyncResultDtoMapper.toDto(syncResult));
    }

    private AppContentRequestContext getRequestContext(boolean dryRun, AppContentResultLogLevel resultLogLevel) {
        return new AppContentRequestContext(configuration.getPartyTypes().getIncluded(),
                                            configuration.getRelationTypes().getIncluded(),
                                            dryRun,
                                            resultLogLevel);
    }
}
