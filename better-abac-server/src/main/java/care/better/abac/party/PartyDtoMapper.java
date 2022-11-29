package care.better.abac.party;

import care.better.abac.content.AppContentCache;
import care.better.abac.content.AppContentRequestContext;
import care.better.abac.dto.content.PlainPartyDto;
import care.better.abac.dto.content.PlainPartyTypeDto;
import care.better.abac.exception.AbacAppContentSyncExceptions;
import care.better.abac.jpa.AbstractPlainDtoMapper;
import care.better.abac.jpa.entity.Party;
import care.better.abac.jpa.entity.PartyType;
import care.better.abac.rest.MappingUtils;

/**
 * @author Matic Ribic
 */
public class PartyDtoMapper extends AbstractPlainDtoMapper<PlainPartyDto, Party> {
    private final PartyTypeDtoMapper partyTypeDtoMapper;

    public PartyDtoMapper(AppContentCache cache) {
        super(cache);
        partyTypeDtoMapper = new PartyTypeDtoMapper(cache);
    }

    @Override
    public PlainPartyDto toPlainDto(Party entity) {
        PlainPartyDto dto = new PlainPartyDto();
        dto.setType(entity.getType().getName());
        MappingUtils.synchronizeSets(entity.getExternalIds(), dto.getExternalIds());
        return dto;
    }

    @Override
    public Party createEntity(PlainPartyDto dto, boolean dryRun) {
        Party entity = new Party(dryRun ? generateNextId(Party.class) : null);
        entity.setType(getCache().getByName(PartyType.class, dto.getType()));
        MappingUtils.synchronizeSets(dto.getExternalIds(), entity.getExternalIds());
        return entity;
    }

    @Override
    public boolean doKeysMatch(PlainPartyDto dto, Party entity) {
        return entity.getExternalIds().equals(dto.getExternalIds()) && partyTypeDtoMapper.doKeysMatch(new PlainPartyTypeDto(dto.getType()), entity.getType());
    }

    @Override
    public boolean isChanged(PlainPartyDto dto, Party entity) {
        return false;
    }

    @Override
    public void validateDto(PlainPartyDto dto, AppContentRequestContext context) {
        if (!context.isPartyTypeIncluded(dto.getType())) {
            throw AbacAppContentSyncExceptions.partyOfUnsupportedType(dto);
        }

        if (!getCache().containsName(PartyType.class, dto.getType())) {
            throw AbacAppContentSyncExceptions.partyOfMissingPartyType(dto);
        }
    }

    @Override
    protected Class<Party> getEntityType() {
        return Party.class;
    }
}
