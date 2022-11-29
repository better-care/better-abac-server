package care.better.abac.party;

import care.better.abac.content.AbstractContentSyncStep;
import care.better.abac.content.AppContentCache;
import care.better.abac.content.AppContentRequestContext;
import care.better.abac.dto.content.PlainPartyTypeDto;
import care.better.abac.jpa.PlainDtoMapper;
import care.better.abac.jpa.QueryListFilter;
import care.better.abac.jpa.entity.EntityWithId;
import care.better.abac.jpa.entity.PartyType;
import care.better.abac.jpa.repo.PartyTypeRepository;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * @author Matic Ribic
 */
public class PartyTypeSyncStep extends AbstractContentSyncStep<PlainPartyTypeDto, PartyType> {
    private final PartyTypeRepository partyTypeRepository;

    public PartyTypeSyncStep(PartyTypeRepository partyTypeRepository) {
        super(partyTypeRepository);
        this.partyTypeRepository = partyTypeRepository;
    }

    @Override
    public List<PartyType> getExistingEntities(AppContentRequestContext context, AppContentCache cache) {
        return partyTypeRepository.findAllByNames(QueryListFilter.of(context.getIncludedPartyTypes(), true));
    }

    @Override
    protected PlainDtoMapper<PlainPartyTypeDto, PartyType> getDtoMapper(AppContentCache cache) {
        return new PartyTypeDtoMapper(cache);
    }

    @Override
    public Class<PlainPartyTypeDto> getPlainDtoType() {
        return PlainPartyTypeDto.class;
    }

    @Override
    public Class<PartyType> getEntityType() {
        return PartyType.class;
    }

    @Override
    public Set<Class<? extends EntityWithId>> getDependentEntityTypes() {
        return Collections.emptySet();
    }
}
