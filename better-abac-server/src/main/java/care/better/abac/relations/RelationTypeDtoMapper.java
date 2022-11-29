package care.better.abac.relations;

import care.better.abac.content.AppContentCache;
import care.better.abac.content.AppContentRequestContext;
import care.better.abac.dto.content.PlainRelationTypeDto;
import care.better.abac.exception.AbacAppContentSyncExceptions;
import care.better.abac.jpa.AbstractPlainDtoMapper;
import care.better.abac.jpa.entity.PartyType;
import care.better.abac.jpa.entity.RelationType;

import java.util.Objects;

/**
 * @author Matic Ribic
 */
public class RelationTypeDtoMapper extends AbstractPlainDtoMapper<PlainRelationTypeDto, RelationType> {

    public RelationTypeDtoMapper(AppContentCache cache) {
        super(cache);
    }

    @Override
    public PlainRelationTypeDto toPlainDto(RelationType entity) {
        PlainRelationTypeDto dto = new PlainRelationTypeDto();
        dto.setName(entity.getName());

        dto.setAllowedSourcePartyType(entity.getAllowedSource().getName());
        dto.setAllowedTargetPartyType(entity.getAllowedTarget().getName());

        return dto;
    }

    @Override
    public RelationType createEntity(PlainRelationTypeDto dto, boolean dryRun) {
        RelationType entity = new RelationType(dryRun ? generateNextId(RelationType.class) : null);
        entity.setName(dto.getName());

        entity.setAllowedSource(getCache().getByName(PartyType.class, dto.getAllowedSourcePartyType()));
        entity.setAllowedTarget(getCache().getByName(PartyType.class, dto.getAllowedTargetPartyType()));

        return entity;
    }

    @Override
    public boolean doKeysMatch(PlainRelationTypeDto dto, RelationType entity) {
        return entity.getName().equals(dto.getName());
    }

    @Override
    public boolean isChanged(PlainRelationTypeDto dto, RelationType entity) {
        return !Objects.equals(dto.getAllowedSourcePartyType(), entity.getAllowedSource().getName()) ||
                !Objects.equals(dto.getAllowedTargetPartyType(), entity.getAllowedTarget().getName());
    }

    @Override
    public void validateDto(PlainRelationTypeDto dto, AppContentRequestContext context) {
        if (!context.isRelationTypeIncluded(dto.getName())) {
            throw AbacAppContentSyncExceptions.relationOfUnsupportedType(dto.getName());
        }
    }

    @Override
    protected Class<RelationType> getEntityType() {
        return RelationType.class;
    }
}
