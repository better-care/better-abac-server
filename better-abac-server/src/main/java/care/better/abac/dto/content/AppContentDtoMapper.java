package care.better.abac.dto.content;

import care.better.abac.content.AppContent;

/**
 * @author Matic Ribic
 */
public final class AppContentDtoMapper {

    private AppContentDtoMapper() {
    }

    public static AppContentDto toDto(AppContent content) {
        AppContentDto dto = new AppContentDto();
        dto.setPartyTypes(content.getDtos(PlainPartyTypeDto.class));
        dto.setParties(content.getDtos(PlainPartyDto.class));
        dto.setRelationTypes(content.getDtos(PlainRelationTypeDto.class));
        dto.setPartyRelations(content.getDtos(PlainPartyRelationDto.class));
        dto.setPolicies(content.getDtos(PlainPolicyDto.class));

        return dto;
    }

    public static AppContent toModel(AppContentDto dto) {
        AppContent content = new AppContent();
        content.setDtos(PlainPartyTypeDto.class, dto.getPartyTypes());
        content.setDtos(PlainPartyDto.class, dto.getParties());
        content.setDtos(PlainRelationTypeDto.class, dto.getRelationTypes());
        content.setDtos(PlainPartyRelationDto.class, dto.getPartyRelations());
        content.setDtos(PlainPolicyDto.class, dto.getPolicies());

        return content;
    }
}
