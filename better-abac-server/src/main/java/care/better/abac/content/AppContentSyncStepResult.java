package care.better.abac.content;

import care.better.abac.dto.content.DtoSyncResult;
import care.better.abac.dto.content.PlainDto;
import care.better.abac.jpa.entity.EntityWithId;
import com.google.common.collect.ImmutableList;

import java.util.List;

/**
 * @author Matic Ribic
 */
public class AppContentSyncStepResult<T extends PlainDto, U extends EntityWithId> {
    private final List<DtoSyncResult<T>> results;
    private final List<U> lazyDeletedEntities;

    public AppContentSyncStepResult(List<DtoSyncResult<T>> results, List<U> lazyDeletedEntities) {
        this.results = ImmutableList.copyOf(results);
        this.lazyDeletedEntities = ImmutableList.copyOf(lazyDeletedEntities);
    }

    public List<DtoSyncResult<T>> getResults() {
        return results;
    }

    public List<U> getLazyDeletedEntities() {
        return lazyDeletedEntities;
    }
}
