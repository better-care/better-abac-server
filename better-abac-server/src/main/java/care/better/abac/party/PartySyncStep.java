package care.better.abac.party;

import care.better.abac.content.AbstractContentSyncStep;
import care.better.abac.content.AppContentCache;
import care.better.abac.content.AppContentRequestContext;
import care.better.abac.dto.content.PlainPartyDto;
import care.better.abac.jpa.PlainDtoMapper;
import care.better.abac.jpa.entity.EntityWithId;
import care.better.abac.jpa.entity.Party;
import care.better.abac.jpa.entity.PartyType;
import care.better.abac.jpa.repo.PartyRepository;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * @author Matic Ribic
 */
public class PartySyncStep extends AbstractContentSyncStep<PlainPartyDto, Party> {
    private final PartyRepository partyRepository;

    public PartySyncStep(PartyRepository partyRepository) {
        super(partyRepository);
        this.partyRepository = partyRepository;
    }

    @Override
    public List<Party> getExistingEntities(AppContentRequestContext context, AppContentCache cache) {
        return partyRepository.findAllByTypeIds(cache.getAllIds(PartyType.class));
    }

    @Override
    protected PlainDtoMapper<PlainPartyDto, Party> getDtoMapper(AppContentCache cache) {
        return new PartyDtoMapper(cache);
    }

    @Override
    public Class<PlainPartyDto> getPlainDtoType() {
        return PlainPartyDto.class;
    }

    @Override
    public Class<Party> getEntityType() {
        return Party.class;
    }

    @Override
    public Set<Class<? extends EntityWithId>> getDependentEntityTypes() {
        return Collections.singleton(PartyType.class);
    }
}
