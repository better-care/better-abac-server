package care.better.abac.party;

import care.better.abac.content.AppContentCache;
import care.better.abac.content.AppContentRequestContext;
import care.better.abac.dto.content.PlainPartyTypeDto;
import care.better.abac.exception.AbacAppContentSyncExceptions;
import care.better.abac.jpa.AbstractPlainDtoMapper;
import care.better.abac.jpa.entity.PartyType;

/**
 * @author Matic Ribic
 */
public class PartyTypeDtoMapper extends AbstractPlainDtoMapper<PlainPartyTypeDto, PartyType> {

    public PartyTypeDtoMapper(AppContentCache cache) {
        super(cache);
    }

    @Override
    public PlainPartyTypeDto toPlainDto(PartyType entity) {
        PlainPartyTypeDto dto = new PlainPartyTypeDto();
        dto.setName(entity.getName());
        return dto;
    }

    @Override
    public PartyType createEntity(PlainPartyTypeDto dto, boolean dryRun) {
        PartyType entity = new PartyType(dryRun ? generateNextId(PartyType.class) : null);
        entity.setName(dto.getName());
        return entity;
    }

    @Override
    public boolean doKeysMatch(PlainPartyTypeDto dto, PartyType entity) {
        return entity.getName().equals(dto.getName());
    }

    @Override
    public boolean isChanged(PlainPartyTypeDto dto, PartyType entity) {
        return false;
    }

    @Override
    public void validateDto(PlainPartyTypeDto dto, AppContentRequestContext context) {
        if (!context.isPartyTypeIncluded(dto.getName())) {
            throw AbacAppContentSyncExceptions.unsupportedPartyType(dto.getName());
        }
    }

    @Override
    protected Class<PartyType> getEntityType() {
        return PartyType.class;
    }
}
