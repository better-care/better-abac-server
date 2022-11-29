package care.better.abac.dto.content;

import care.better.abac.jpa.entity.EntityWithId;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Matic Ribic
 */
public class PlainDtoCache {
    private final Map<Class<? extends EntityWithId>, Map<Long, PlainDto>> plainDtoCacheByTypeAndId = new HashMap<>();

    public <T extends EntityWithId, U extends PlainDto> void put(T entity, U plainDto) {
        plainDtoCacheByTypeAndId.computeIfAbsent(entity.getClass(), ignored -> new HashMap<>()).put(entity.getId(), plainDto);
    }

    public <T extends EntityWithId, U extends PlainDto> U get(T entity) {
        return (U)plainDtoCacheByTypeAndId.getOrDefault(entity.getClass(), Collections.emptyMap()).get(entity.getId());
    }
}
