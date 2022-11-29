package care.better.abac.content;

import care.better.abac.jpa.entity.EntityWithId;
import care.better.abac.jpa.entity.Named;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author Matic Ribic
 */
public class AppContentCache {
    private final Map<Class<? extends EntityWithId>, Map<Long, EntityWithId>> cacheByTypeAndId = new HashMap<>();
    private final Map<Class<? extends Named>, Map<String, EntityWithId>> cacheByTypeAndName = new HashMap<>();

    public <T extends EntityWithId> List<T> getAll(Class<T> type) {
        return ImmutableList.copyOf(cacheByTypeAndId.getOrDefault(type, Collections.emptyMap()).values().stream().map(type::cast).collect(Collectors.toList()));
    }

    public <T extends EntityWithId> Set<Long> getAllIds(Class<T> type) {
        return ImmutableSet.copyOf(cacheByTypeAndId.getOrDefault(type, Collections.emptyMap()).keySet());
    }

    public <T extends EntityWithId> boolean contains(Class<T> type, Predicate<T> predicate) {
        return getBy(type, predicate).isPresent();
    }

    public <T extends EntityWithId> Optional<T> getBy(Class<T> type, Predicate<T> predicate) {
        return cacheByTypeAndId.containsKey(type)
                ? cacheByTypeAndId.get(type).values().stream().map(type::cast).filter(predicate).findFirst()
                : Optional.empty();
    }

    public <T extends Named> boolean containsName(Class<T> type, String name) {
        return cacheByTypeAndName.containsKey(type) && cacheByTypeAndName.get(type).containsKey(name);
    }

    public <T extends Named> T getByName(Class<T> type, String name) {
        return cacheByTypeAndName.containsKey(type) ? (T)cacheByTypeAndName.get(type).get(name) : null;
    }

    public <T extends EntityWithId> void put(T entity) {
        cacheByTypeAndId.computeIfAbsent(entity.getClass(), ignored -> new HashMap<>()).put(entity.getId(), entity);
        if (entity instanceof Named) {
            Named named = (Named)entity;
            cacheByTypeAndName.computeIfAbsent(named.getClass(), ignored -> new HashMap<>()).put(named.getName(), entity);
        }
    }

    public <T extends EntityWithId> void remove(T entity) {
        cacheByTypeAndId.getOrDefault(entity.getClass(), Collections.emptyMap()).remove(entity.getId());
        if (entity instanceof Named) {
            Named named = (Named)entity;
            cacheByTypeAndName.getOrDefault(named.getClass(), Collections.emptyMap()).remove(named.getName());
        }
    }

    public <T extends EntityWithId> void putAll(Collection<T> dtos) {
        dtos.forEach(this::put);
    }
}
