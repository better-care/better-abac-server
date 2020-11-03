package care.better.abac.dto;

/**
 * @author Bostjan Lah
 */
public class RelationTypeDto extends NamedDtoWithId {
    private String allowedSourcePartyType;
    private String allowedTargetPartyType;

    public RelationTypeDto() {
    }

    public RelationTypeDto(Long id, String name) {
        super(id, name);
    }

    public String getAllowedSourcePartyType() {
        return allowedSourcePartyType;
    }

    public void setAllowedSourcePartyType(String allowedSourcePartyType) {
        this.allowedSourcePartyType = allowedSourcePartyType;
    }

    public String getAllowedTargetPartyType() {
        return allowedTargetPartyType;
    }

    public void setAllowedTargetPartyType(String allowedTargetPartyType) {
        this.allowedTargetPartyType = allowedTargetPartyType;
    }
}
