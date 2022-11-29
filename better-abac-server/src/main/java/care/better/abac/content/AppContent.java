package care.better.abac.content;

import care.better.abac.dto.content.PlainDto;
import com.google.common.collect.ImmutableList;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Matic Ribic
 */
public class AppContent {
    private final Map<Class<? extends PlainDto>, List<PlainDto>> dtoByTypes = new HashMap<>();

    public Set<Class<? extends PlainDto>> getDtoTypes() {
        return dtoByTypes.keySet();
    }

    public <T extends PlainDto> List<T> getDtos(Class<T> dtoType) {
        return ImmutableList.copyOf(dtoByTypes.getOrDefault(dtoType, Collections.emptyList()).stream().map(dtoType::cast).collect(Collectors.toList()));
    }

    public <T extends PlainDto> void setDtos(Class<T> dtoType, List<T> dtos) {
        dtoByTypes.put(dtoType, ImmutableList.copyOf(dtos));
    }

    public boolean isEmpty() {
        return dtoByTypes.values().isEmpty() || dtoByTypes.values().stream().allMatch(Collection::isEmpty);
    }

    @Override
    public String toString() {
        return String.format("AppContent{dtoByTypes=%s}", dtoByTypes);
    }
}
