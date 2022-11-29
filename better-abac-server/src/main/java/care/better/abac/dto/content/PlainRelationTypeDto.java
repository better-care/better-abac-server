package care.better.abac.dto.content;

import java.util.Objects;

/**
 * @author Bostjan Lah
 */
public class PlainRelationTypeDto extends NamedPlainDto {
    private String allowedSourcePartyType;
    private String allowedTargetPartyType;

    public PlainRelationTypeDto() {
    }

    public PlainRelationTypeDto(String name) {
        super(name);
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PlainRelationTypeDto)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        PlainRelationTypeDto that = (PlainRelationTypeDto)o;
        return Objects.equals(allowedSourcePartyType, that.allowedSourcePartyType) && Objects.equals(allowedTargetPartyType,
                                                                                                     that.allowedTargetPartyType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), allowedSourcePartyType, allowedTargetPartyType);
    }

    @Override
    public String toString() {
        return String.format("PlainRelationTypeDto{name='%s', allowedSourcePartyType='%s', allowedTargetPartyType='%s'}",
                             getName(),
                             allowedSourcePartyType,
                             allowedTargetPartyType);
    }
}
