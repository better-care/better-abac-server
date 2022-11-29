package care.better.abac.content;

import care.better.abac.dto.content.DtoSyncResult;
import care.better.abac.dto.content.PlainDto;
import care.better.abac.dto.content.PlainDtoCache;
import care.better.abac.jpa.entity.EntityWithId;

import java.util.List;
import java.util.Set;

/**
 * @author Matic Ribic
 */
public interface AppContentSyncStep<T extends PlainDto, U extends EntityWithId> {

    List<T> retrieve(AppContentRequestContext context, AppContentCache cache, PlainDtoCache plainDtoCache);

    default List<DtoSyncResult<T>> submit(List<T> submittedPlainDtos, AppContentRequestContext context, AppContentCache cache, PlainDtoCache plainDtoCache) {
        return submit(submittedPlainDtos, context, cache, plainDtoCache, false).getResults();
    }

    AppContentSyncStepResult<T, U> submit(
            List<T> submittedPlainDtos,
            AppContentRequestContext context,
            AppContentCache cache,
            PlainDtoCache plainDtoCache,
            boolean lazyDelete);

    void delete(List<U> entities, AppContentRequestContext context, PlainDtoCache plainDtoCache);

    Class<T> getPlainDtoType();

    Class<U> getEntityType();

    Set<Class<? extends EntityWithId>> getDependentEntityTypes();
}
