package care.better.abac.dto;

import java.time.OffsetDateTime;
import java.util.Objects;

/**
 * @author Bostjan Lah
 */
public class PartyRelationDto extends DtoWithId {
    private Long source;
    private String relationType;
    private Long target;
    private OffsetDateTime validUntil;

    public PartyRelationDto() {
    }

    public PartyRelationDto(Long id, Long source, String relationType, Long target) {
        super(id);
        this.source = source;
        this.target = target;
        this.relationType = relationType;
    }

    public Long getSource() {
        return source;
    }

    public void setSource(Long source) {
        this.source = source;
    }

    public String getRelationType() {
        return relationType;
    }

    public void setRelationType(String relationType) {
        this.relationType = relationType;
    }

    public Long getTarget() {
        return target;
    }

    public void setTarget(Long target) {
        this.target = target;
    }

    public OffsetDateTime getValidUntil() {
        return validUntil;
    }

    public void setValidUntil(OffsetDateTime validUntil) {
        this.validUntil = validUntil;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PartyRelationDto that = (PartyRelationDto)o;
        return Objects.equals(source, that.source) &&
                Objects.equals(relationType, that.relationType) &&
                Objects.equals(target, that.target) &&
                Objects.equals(validUntil, that.validUntil);
    }

    @Override
    public int hashCode() {
        return Objects.hash(source, relationType, target, validUntil);
    }
}
