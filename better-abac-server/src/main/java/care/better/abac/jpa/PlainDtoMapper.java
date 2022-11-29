package care.better.abac.jpa;

import care.better.abac.content.AppContentRequestContext;
import care.better.abac.dto.content.PlainDto;
import care.better.abac.jpa.entity.EntityWithId;

/**
 * @author Matic Ribic
 */
public interface PlainDtoMapper<T extends PlainDto, U extends EntityWithId> {

    T toPlainDto(U entity);

    default U toEntity(T dto) {
        return toEntity(dto, false);
    }

    U toEntity(T dto, boolean dryRun);

    boolean isEqual(T dto, U entity);

    boolean doKeysMatch(T dto, U entity);

    boolean isChanged(T dto, U entity);

    void validateDto(T dto, AppContentRequestContext context);
}
