package care.better.abac.content;

import care.better.abac.dto.content.DtoSyncResult;
import care.better.abac.dto.content.DtoSyncResultState;
import care.better.abac.dto.content.PlainDto;
import care.better.abac.dto.content.PlainDtoCache;
import care.better.abac.jpa.EntityCrudService;
import care.better.abac.jpa.PlainDtoMapper;
import care.better.abac.jpa.RepositoryAsEntityCrudService;
import care.better.abac.jpa.entity.EntityWithId;
import care.better.abac.jpa.repo.EntityCrudRepository;
import com.google.common.base.Preconditions;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Matic Ribic
 */
public abstract class AbstractContentSyncStep<T extends PlainDto, U extends EntityWithId> implements AppContentSyncStep<T, U> {
    private static final Logger log = LogManager.getLogger(AbstractContentSyncStep.class);

    private final EntityCrudService<U> crudService;

    private static final String RETRIEVE_ACTION = "retrieve";
    private static final String SUBMIT_ACTION = "submit";

    protected AbstractContentSyncStep(EntityCrudRepository<U> repository) {
        crudService = new RepositoryAsEntityCrudService<>(repository);
    }

    protected AbstractContentSyncStep(EntityCrudService<U> crudService) {
        this.crudService = crudService;
    }

    protected abstract List<U> getExistingEntities(AppContentRequestContext context, AppContentCache cache);

    protected abstract PlainDtoMapper<T, U> getDtoMapper(AppContentCache cache);

    @Override
    public List<T> retrieve(AppContentRequestContext context, AppContentCache cache, PlainDtoCache plainDtoCache) {
        for (Class<? extends EntityWithId> dependentEntityType : getDependentEntityTypes()) {
            Preconditions.checkArgument(context.isVisited(dependentEntityType, RETRIEVE_ACTION),
                                        "Try to submit content in invalid order of sync steps! Data hasn't been retrieved yet for dependent entity type " + dependentEntityType.getName());
        }

        PlainDtoMapper<T, U> dtoMapper = getDtoMapper(cache);

        List<T> dtos = getExistingEntities(context, cache).stream()
                .map(entity -> {
                    T plainDto = dtoMapper.toPlainDto(entity);
                    cache.put(entity);
                    if (plainDtoCache != null) {
                        plainDtoCache.put(entity, plainDto);
                    }
                    return plainDto;
                })
                .collect(Collectors.toList());

        context.markAsVisited(getEntityType(), RETRIEVE_ACTION);
        return dtos;
    }

    @Override
    @Transactional
    public AppContentSyncStepResult<T, U> submit(
            List<T> submittedPlainDtos,
            AppContentRequestContext context,
            AppContentCache cache,
            PlainDtoCache plainDtoCache,
            boolean lazyDelete) {
        Preconditions.checkArgument(context.isVisited(getEntityType(), RETRIEVE_ACTION),
                                    "Try to submit content before existing data has been retrieved for entity type " + getEntityType().getName() + "!");
        getDependentEntityTypes().forEach(dependentEntityType -> {
            Preconditions.checkArgument(context.isVisited(dependentEntityType, RETRIEVE_ACTION),
                                        "Try to submit content in invalid order of sync steps! Data hasn't been retrieved yet for dependent entity type " + dependentEntityType.getName() + "!");
            Preconditions.checkArgument(context.isVisited(dependentEntityType, SUBMIT_ACTION),
                                        "Try to submit content in invalid order of sync steps! Data hasn't been submitted yet for dependent entity type " + dependentEntityType.getName() + "!");
        });

        EntityCrudService<U> actualService = context.isDryRun() ? new NoopEntityCrudService<>() : crudService;

        List<U> existingEntities = new ArrayList<>(cache.getAll(getEntityType()));

        PlainDtoMapper<T, U> dtoMapper = getDtoMapper(cache);
        List<DtoSyncResult<T>> syncResults = new ArrayList<>();
        for (T submittedPlainDto : submittedPlainDtos) {
            dtoMapper.validateDto(submittedPlainDto, context);

            U submittedEntity = dtoMapper.toEntity(submittedPlainDto, context.isDryRun());
            Optional<U> existingEntityOptional = existingEntities.stream()
                    .filter(entity -> dtoMapper.doKeysMatch(submittedPlainDto, entity))
                    .findFirst();
            if (existingEntityOptional.isPresent()) {
                U existingEntity = existingEntityOptional.get();
                existingEntities.remove(existingEntity);

                // update if changed
                if (dtoMapper.isChanged(submittedPlainDto, existingEntity)) {
                    Optional<U> updatedDtoOptional = actualService.update(existingEntity.getId(), submittedEntity);
                    if (updatedDtoOptional.isPresent()) {
                        U updatedEntity = updatedDtoOptional.get();
                        cache.put(updatedEntity);
                        T existingPlainDto = plainDtoCache.get(existingEntity);
                        addResult(syncResults, DtoSyncResult.updated(submittedPlainDto, existingPlainDto), context);
                        log.info(String.format("%s updated the existing %s.", submittedPlainDto, existingPlainDto));
                    } else {
                        throw new EmptyResultDataAccessException(String.format("No %s entity with id %s exists! Update failed",
                                                                               existingEntity.getClass(),
                                                                               existingEntity.getId()), 1);
                    }
                } else {
                    log.debug(() -> submittedPlainDto + " is unchanged.");
                    cache.put(existingEntity);
                    addResult(syncResults, DtoSyncResult.unmodified(submittedPlainDto), context);
                }
            } else {
                // create
                U createdEntity = actualService.create(submittedEntity);
                cache.put(createdEntity);
                addResult(syncResults, DtoSyncResult.created(submittedPlainDto), context);
                log.info("Created new {}", submittedPlainDto);
            }
        }

        // delete
        List<U> lazyDeleteEntities = new ArrayList<>();
        existingEntities.forEach(entityToDelete -> {
            T plainDeletedDto = plainDtoCache.get(entityToDelete);
            if (lazyDelete) {
                lazyDeleteEntities.add(entityToDelete);
                log.debug("{} marked for lazy deleting.", plainDeletedDto);
            } else {
                actualService.deleteById(entityToDelete.getId());
                log.info("Created new {}", plainDeletedDto);
            }

            cache.remove(entityToDelete);
            addResult(syncResults, DtoSyncResult.deleted(plainDeletedDto), context);
        });

        context.markAsVisited(getEntityType(), SUBMIT_ACTION);
        return new AppContentSyncStepResult<>(syncResults, lazyDeleteEntities);
    }

    @Override
    public void delete(List<U> entities, AppContentRequestContext context, PlainDtoCache plainDtoCache) {
        if (!context.isDryRun()) {
            entities.forEach(entity -> {
                crudService.deleteById(entity.getId());
                T plainDeletedDto = plainDtoCache.get(entity);
                log.info("{} deleted.", plainDeletedDto);
            });
        }
    }

    private void addResult(List<DtoSyncResult<T>> syncResults, DtoSyncResult<T> syncResult, AppContentRequestContext requestContext) {
        switch (requestContext.getResultLogLevel()) {
            case ALL:
                syncResults.add(syncResult);
                return;
            case CHANGES_ONLY:
                if (syncResult.getState() != DtoSyncResultState.UNMODIFIED) {
                    syncResults.add(syncResult);
                }
                return;
            case NONE:
                return;
            default:
                throw new UnsupportedOperationException("Unknown AppContentResultLogLevel: " + requestContext.getResultLogLevel());
        }
    }
}
