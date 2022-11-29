package care.better.abac.content;

import care.better.abac.dto.content.DtoSyncResult;
import care.better.abac.dto.content.PlainDto;
import care.better.abac.dto.content.PlainDtoCache;
import care.better.abac.jpa.entity.EntityWithId;
import com.google.common.collect.ImmutableList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Matic Ribic
 */
public class AppContentServiceImpl implements AppContentService {
    private static final Logger log = LogManager.getLogger(AppContentServiceImpl.class);

    private final List<AppContentSyncStep<? extends PlainDto, ? extends EntityWithId>> retrieveSteps;
    private final List<AppContentSyncStepDefinition<? extends PlainDto, ? extends EntityWithId>> submitStepDefinitions;

    public AppContentServiceImpl(
            List<AppContentSyncStep<? extends PlainDto, ? extends EntityWithId>> retrieveSteps,
            List<AppContentSyncStepDefinition<? extends PlainDto, ? extends EntityWithId>> submitStepDefinitions) {
        this.retrieveSteps = ImmutableList.copyOf(retrieveSteps);
        this.submitStepDefinitions = ImmutableList.copyOf(submitStepDefinitions);
    }

    @Override
    public AppContent getContent(AppContentRequestContext requestContext) {
        log.debug(() -> String.format("Retrieving content for %s...", requestContext));
        AppContent content = getContentAndFillCache(requestContext, new AppContentCache(), null);

        log.info("Retrieved content [{}] for {}",
                 content.getDtoTypes().stream().map(type -> type.getSimpleName() + ": " + content.getDtos(type).size()).collect(Collectors.joining(", ")),
                 requestContext);
        log.trace(() -> String.format("Retrieved content %s for %s", content, requestContext));
        return content;
    }

    @Override
    @Transactional
    public AppContentSyncResult submitContent(AppContent content, AppContentRequestContext requestContext) {
        log.debug(() -> String.format("Submitting content for %s...", requestContext));
        AppContentSyncResult syncResult = new AppContentSyncResult();

        PlainDtoCache plainDtoCache = new PlainDtoCache();
        AppContentCache cache = new AppContentCache();
        getContentAndFillCache(requestContext, cache, plainDtoCache);

        Map<Class<? extends PlainDto>, List<? extends EntityWithId>> lazyDeletedEntities = new HashMap<>();
        for (AppContentSyncStepDefinition<? extends PlainDto, ? extends EntityWithId> stepDefinition : submitStepDefinitions) {
            String stepTypeName = ClassUtils.getUserClass(stepDefinition.getStep().getClass()).getSimpleName();
            AppContentSyncStepActionType actionType = stepDefinition.getActionType();
            log.debug(() -> String.format("Executing action %s of step %s...", actionType, stepTypeName));
            AppContentSyncStep<? extends PlainDto, ? extends EntityWithId> step = stepDefinition.getStep();
            Class<? extends PlainDto> plainDtoType = step.getPlainDtoType();
            List dtos = content.getDtos(plainDtoType);
            try {
                switch (actionType) {
                    case SUBMIT:
                        List<DtoSyncResult> result = step.submit(dtos, requestContext, cache, plainDtoCache);
                        syncResult.setResults(plainDtoType, result);
                        break;
                    case SUBMIT_WITH_LAZY_DELETE:
                        AppContentSyncStepResult stepResult = step.submit(dtos, requestContext, cache, plainDtoCache, true);
                        syncResult.setResults(plainDtoType, stepResult.getResults());
                        lazyDeletedEntities.put(plainDtoType, stepResult.getLazyDeletedEntities());
                        break;
                    case DELETE:
                        List entitiesToDelete = lazyDeletedEntities.remove(plainDtoType);
                        if (!CollectionUtils.isEmpty(entitiesToDelete)) {
                            step.delete(entitiesToDelete, requestContext, plainDtoCache);
                        }
                        break;
                    default:
                        throw new UnsupportedOperationException(String.format("Unknown action type %s for synchronization step %s.", actionType, stepTypeName));
                }
            } catch (RuntimeException e) {
                log.error("Execution of action {} of step {} failed with error {}: {}. Synchronization will be rolled back.",
                          actionType,
                          stepTypeName,
                          e.getClass(),
                          e.getMessage());
                throw e;
            }
            log.info("Executed action {} of step {}.", actionType, stepTypeName);
        }

        log.info("Submitted content [{}] for {}",
                 content.getDtoTypes().stream().map(type -> type.getSimpleName() + ": " + content.getDtos(type).size()).collect(Collectors.joining(", ")),
                 requestContext);
        log.trace(() -> String.format("Submitted content %s for %s: %s", content, requestContext, syncResult));
        return syncResult;
    }

    private AppContent getContentAndFillCache(AppContentRequestContext requestContext, AppContentCache cache, PlainDtoCache plainDtoCache) {
        AppContent content = new AppContent();
        retrieveSteps.forEach(step -> content.setDtos(step.getPlainDtoType(), (List)step.retrieve(requestContext, cache, plainDtoCache)));
        return content;
    }
}
