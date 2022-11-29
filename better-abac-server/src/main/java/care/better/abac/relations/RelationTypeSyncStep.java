package care.better.abac.relations;

import care.better.abac.content.AbstractContentSyncStep;
import care.better.abac.content.AppContentCache;
import care.better.abac.content.AppContentRequestContext;
import care.better.abac.dto.content.PlainRelationTypeDto;
import care.better.abac.jpa.PlainDtoMapper;
import care.better.abac.jpa.QueryListFilter;
import care.better.abac.jpa.entity.EntityWithId;
import care.better.abac.jpa.entity.PartyType;
import care.better.abac.jpa.entity.RelationType;
import care.better.abac.jpa.repo.RelationTypeRepository;
import com.google.common.collect.ImmutableList;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * @author Matic Ribic
 */
public class RelationTypeSyncStep extends AbstractContentSyncStep<PlainRelationTypeDto, RelationType> {
    private final RelationTypeRepository relationTypeRepository;

    public RelationTypeSyncStep(RelationTypeRepository relationTypeRepository) {
        super(relationTypeRepository);
        this.relationTypeRepository = relationTypeRepository;
    }

    @Override
    public List<RelationType> getExistingEntities(AppContentRequestContext context, AppContentCache cache) {
        Set<Long> partyTypeIds = cache.getAllIds(PartyType.class);
        return partyTypeIds.isEmpty()
                ? ImmutableList.copyOf(relationTypeRepository.findAll())
                : relationTypeRepository.findAllByPartyTypeIds(QueryListFilter.of(context.getIncludedRelationTypes(), true), partyTypeIds);
    }

    @Override
    protected PlainDtoMapper<PlainRelationTypeDto, RelationType> getDtoMapper(AppContentCache cache) {
        return new RelationTypeDtoMapper(cache);
    }

    @Override
    public Class<PlainRelationTypeDto> getPlainDtoType() {
        return PlainRelationTypeDto.class;
    }

    @Override
    public Class<RelationType> getEntityType() {
        return RelationType.class;
    }

    @Override
    public Set<Class<? extends EntityWithId>> getDependentEntityTypes() {
        return Collections.singleton(PartyType.class);
    }
}
