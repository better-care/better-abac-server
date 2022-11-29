package care.better.abac.dto.content;

import care.better.abac.content.AppContentSyncResult;

/**
 * @author Matic Ribic
 */
public final class AppContentSyncResultDtoMapper {

    private AppContentSyncResultDtoMapper() {
    }

    public static AppContentSyncResultDto toDto(AppContentSyncResult syncResult) {
        AppContentSyncResultDto dto = new AppContentSyncResultDto();
        dto.setPartyTypes(syncResult.getResults(PlainPartyTypeDto.class));
        dto.setParties(syncResult.getResults(PlainPartyDto.class));
        dto.setRelationTypes(syncResult.getResults(PlainRelationTypeDto.class));
        dto.setPartyRelations(syncResult.getResults(PlainPartyRelationDto.class));
        dto.setPolicies(syncResult.getResults(PlainPolicyDto.class));

        return dto;
    }

    public static AppContentSyncResult toModel(AppContentSyncResultDto dto) {
        AppContentSyncResult syncResult = new AppContentSyncResult();
        syncResult.setResults(PlainPartyTypeDto.class, dto.getPartyTypes());
        syncResult.setResults(PlainPartyDto.class, dto.getParties());
        syncResult.setResults(PlainRelationTypeDto.class, dto.getRelationTypes());
        syncResult.setResults(PlainPartyRelationDto.class, dto.getPartyRelations());
        syncResult.setResults(PlainPolicyDto.class, dto.getPolicies());

        return syncResult;
    }
}
