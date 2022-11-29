package care.better.abac.relations;

import care.better.abac.content.AbstractContentSyncStep;
import care.better.abac.content.AppContentCache;
import care.better.abac.content.AppContentRequestContext;
import care.better.abac.dto.content.PlainPartyRelationDto;
import care.better.abac.jpa.PlainDtoMapper;
import care.better.abac.jpa.entity.EntityWithId;
import care.better.abac.jpa.entity.Party;
import care.better.abac.jpa.entity.PartyRelation;
import care.better.abac.jpa.entity.RelationType;
import care.better.abac.jpa.repo.PartyRelationRepository;
import com.google.common.collect.ImmutableSet;

import java.util.List;
import java.util.Set;

/**
 * @author Matic Ribic
 */
public class PartyRelationSyncStep extends AbstractContentSyncStep<PlainPartyRelationDto, PartyRelation> {
    private final PartyRelationRepository partyRelationRepository;

    public PartyRelationSyncStep(PartyRelationRepository partyRelationRepository) {
        super(partyRelationRepository);
        this.partyRelationRepository = partyRelationRepository;
    }

    @Override
    public List<PartyRelation> getExistingEntities(AppContentRequestContext context, AppContentCache cache) {
        Set<Long> partyIds = cache.getAllIds(Party.class);
        Set<Long> relationTypeIds = cache.getAllIds(RelationType.class);
        return partyRelationRepository.findAllByPartyAndRelationTypeIds(partyIds, relationTypeIds);
    }

    @Override
    protected PlainDtoMapper<PlainPartyRelationDto, PartyRelation> getDtoMapper(AppContentCache cache) {
        return new PartyRelationDtoMapper(cache);
    }

    @Override
    public Class<PlainPartyRelationDto> getPlainDtoType() {
        return PlainPartyRelationDto.class;
    }

    @Override
    public Class<PartyRelation> getEntityType() {
        return PartyRelation.class;
    }

    @Override
    public Set<Class<? extends EntityWithId>> getDependentEntityTypes() {
        return ImmutableSet.of(Party.class, RelationType.class);
    }
}
