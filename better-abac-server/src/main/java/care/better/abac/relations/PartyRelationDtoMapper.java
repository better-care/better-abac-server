package care.better.abac.relations;

import care.better.abac.content.AppContentCache;
import care.better.abac.content.AppContentRequestContext;
import care.better.abac.dto.content.PlainPartyDto;
import care.better.abac.dto.content.PlainPartyRelationDto;
import care.better.abac.dto.content.PlainRelationTypeDto;
import care.better.abac.exception.AbacAppContentSyncExceptions;
import care.better.abac.jpa.AbstractPlainDtoMapper;
import care.better.abac.jpa.entity.Party;
import care.better.abac.jpa.entity.PartyRelation;
import care.better.abac.jpa.entity.RelationType;
import care.better.abac.party.PartyDtoMapper;
import care.better.core.TimeUtils;

/**
 * @author Matic Ribic
 */
public class PartyRelationDtoMapper extends AbstractPlainDtoMapper<PlainPartyRelationDto, PartyRelation> {
    private final PartyDtoMapper partyDtoMapper;
    private final RelationTypeDtoMapper relationTypeDtoMapper;

    public PartyRelationDtoMapper(AppContentCache cache) {
        super(cache);
        partyDtoMapper = new PartyDtoMapper(cache);
        relationTypeDtoMapper = new RelationTypeDtoMapper(cache);
    }

    @Override
    public PlainPartyRelationDto toPlainDto(PartyRelation entity) {
        PlainPartyRelationDto dto = new PlainPartyRelationDto();

        dto.setSource(partyDtoMapper.toPlainDto(entity.getSource()));
        dto.setRelationType(entity.getRelationType().getName());
        dto.setTarget(partyDtoMapper.toPlainDto(entity.getTarget()));

        dto.setValidUntil(entity.getValidUntil());
        return dto;
    }

    @Override
    public PartyRelation createEntity(PlainPartyRelationDto dto, boolean dryRun) {
        PartyRelation entity = new PartyRelation(dryRun ? generateNextId(PartyRelation.class) : null);

        entity.setSource(partyDtoMapper.toEntity(dto.getSource(), dryRun));
        entity.setRelationType(getCache().getByName(RelationType.class, dto.getRelationType()));
        entity.setTarget(partyDtoMapper.toEntity(dto.getTarget(), dryRun));

        entity.setValidUntil(dto.getValidUntil());
        return entity;
    }

    @Override
    public boolean doKeysMatch(PlainPartyRelationDto dto, PartyRelation entity) {
        return relationTypeDtoMapper.doKeysMatch(new PlainRelationTypeDto(dto.getRelationType()), entity.getRelationType()) &&
                partyDtoMapper.doKeysMatch(dto.getSource(), entity.getSource()) &&
                partyDtoMapper.doKeysMatch(dto.getTarget(), entity.getTarget());
    }

    @Override
    public boolean isChanged(PlainPartyRelationDto dto, PartyRelation entity) {
        return !TimeUtils.equalOffsetDateTime(dto.getValidUntil(), entity.getValidUntil());
    }

    @Override
    protected Class<PartyRelation> getEntityType() {
        return PartyRelation.class;
    }

    @Override
    public void validateDto(PlainPartyRelationDto dto, AppContentRequestContext context) {
        validateParty(dto, dto.getSource(), context);
        validateParty(dto, dto.getTarget(), context);

        String relationType = dto.getRelationType();
        if (!context.isRelationTypeIncluded(relationType)) {
            throw AbacAppContentSyncExceptions.partyRelationOfUnsupportedRelationType(dto, relationType);
        }

        if (!getCache().containsName(RelationType.class, relationType)) {
            throw AbacAppContentSyncExceptions.partyRelationOfMissingRelationType(dto, relationType);
        }
    }

    private void validateParty(PlainPartyRelationDto dto, PlainPartyDto partyDto, AppContentRequestContext context) {
        if (!context.isPartyTypeIncluded(partyDto.getType())) {
            throw AbacAppContentSyncExceptions.partyRelationOfUnsupportedParty(dto, partyDto.getType());
        }

        if (!getCache().contains(Party.class, entity -> partyDtoMapper.isEqual(partyDto, entity))) {
            throw AbacAppContentSyncExceptions.partyRelationOfMissingParty(dto, partyDto);
        }
    }
}
