package care.better.abac.exception;

import care.better.abac.dto.content.PlainDto;
import care.better.abac.dto.content.PlainPartyDto;
import care.better.abac.dto.content.PlainPartyRelationDto;

/**
 * @author Matic Ribic
 */
public final class AbacAppContentSyncExceptions {

    private AbacAppContentSyncExceptions() {
    }

    public static AppContentSyncException unsupportedPartyType(String type) {
        return new AppContentSyncException("SYNC-ABAC-001", "Unsupported party type " + type + " cannot be synchronized!");
    }

    public static AppContentSyncException partyOfUnsupportedType(PlainPartyDto party) {
        return new AppContentSyncException("SYNC-ABAC-010",
                                           String.format("Party with external ids %s of unsupported type %s cannot be synchronized!",
                                                         party.getExternalIds(),
                                                         party.getType()));
    }

    public static AppContentSyncException partyOfMissingPartyType(PlainPartyDto party) {
        return new AppContentSyncException("SYNC-ABAC-011",
                                           String.format("Party with external ids %s of missing type %s cannot be synchronized!",
                                                         party.getExternalIds(),
                                                         party.getType()));
    }

    public static AppContentSyncException relationOfUnsupportedType(String type) {
        return new AppContentSyncException("SYNC-ABAC-020", "Unsupported relation type " + type + " cannot be synchronized!");
    }

    public static AppContentSyncException partyRelationOfUnsupportedRelationType(PlainPartyRelationDto partyRelationDto, String relationType) {
        return new AppContentSyncException("SYNC-ABAC-030",
                                           String.format("%s of unsupported type %s cannot be synchronized!",
                                                         sanitize(partyRelationDto),
                                                         relationType));
    }

    public static AppContentSyncException partyRelationOfMissingRelationType(PlainPartyRelationDto partyRelationDto, String relationType) {
        return new AppContentSyncException("SYNC-ABAC-031",
                                           String.format("%s of missing type %s cannot be synchronized!",
                                                         sanitize(partyRelationDto),
                                                         relationType));
    }

    public static AppContentSyncException partyRelationOfUnsupportedParty(PlainPartyRelationDto partyRelationDto, String partyType) {
        return new AppContentSyncException("SYNC-ABAC-032",
                                           String.format("%s with party of unsupported type %s cannot be synchronized!",
                                                         sanitize(partyRelationDto),
                                                         partyType));
    }

    public static AppContentSyncException partyRelationOfMissingParty(PlainPartyRelationDto partyRelationDto, PlainPartyDto partyDto) {
        return new AppContentSyncException("SYNC-ABAC-033",
                                           String.format("%s with missing party %s cannot be synchronized!",
                                                         sanitize(partyRelationDto),
                                                         sanitize(partyDto)));
    }

    private static String sanitize(PlainDto plainDto) {
        return plainDto.toString()
                .replaceAll("PlainPartyTypeDto", "PartyType")
                .replaceAll("PlainPartyDto", "Party")
                .replaceAll("PlainRelationTypeDto", "RelationType")
                .replaceAll("PlainPartyRelationDto", "PartyRelation");
    }
}
