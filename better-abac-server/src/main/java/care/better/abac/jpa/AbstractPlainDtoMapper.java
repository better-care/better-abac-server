package care.better.abac.jpa;

import care.better.abac.content.AppContentCache;
import care.better.abac.dto.content.PlainDto;
import care.better.abac.jpa.entity.EntityWithId;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Matic Ribic
 */
public abstract class AbstractPlainDtoMapper<T extends PlainDto, U extends EntityWithId> implements PlainDtoMapper<T, U> {
    private final AppContentCache cache;

    @SuppressWarnings("StaticCollection")
    private static final Map<Class<? extends EntityWithId>, AtomicLong> ID_COUNTERS = Collections.synchronizedMap(new HashMap<>());

    protected AbstractPlainDtoMapper(AppContentCache cache) {
        this.cache = cache;
    }

    protected AppContentCache getCache() {
        return cache;
    }

    protected long generateNextId(Class<U> type) {
        return ID_COUNTERS.computeIfAbsent(type, ignored -> new AtomicLong(-1L)).getAndDecrement();
    }

    @Override
    public U toEntity(T dto, boolean dryRun) {
        return cache.getBy(getEntityType(), entity -> isEqual(dto, entity)).orElseGet(() -> createEntity(dto, dryRun));
    }

    @Override
    public boolean isEqual(T dto, U entity) {
        return doKeysMatch(dto, entity) && !isChanged(dto, entity);
    }

    protected abstract U createEntity(T dto, boolean dryRun);

    protected abstract Class<U> getEntityType();
}
