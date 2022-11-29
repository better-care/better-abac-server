package care.better.abac.content;

import care.better.abac.dto.content.DtoSyncResult;
import care.better.abac.dto.content.PlainDto;
import com.google.common.collect.ImmutableList;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Matic Ribic
 */
public class AppContentSyncResult {
    private final Map<Class<? extends PlainDto>, List<DtoSyncResult<? extends PlainDto>>> resultsByDtoTypes = new HashMap<>();

    public <T extends PlainDto, U extends DtoSyncResult<T>> List<U> getResults(Class<T> dtoType) {
        return ImmutableList.copyOf((List<U>)resultsByDtoTypes.getOrDefault(dtoType, Collections.emptyList()));
    }

    public <T extends PlainDto, U extends DtoSyncResult<T>> void setResults(Class<T> dtoType, List<U> results) {
        resultsByDtoTypes.put(dtoType, ImmutableList.copyOf(results));
    }

    public boolean isEmpty() {
        return resultsByDtoTypes.values().isEmpty() || resultsByDtoTypes.values().stream().allMatch(Collection::isEmpty);
    }

    @Override
    public String toString() {
        return String.format("AppContentSyncResult{resultsByDtoTypes=%s}", resultsByDtoTypes);
    }
}
