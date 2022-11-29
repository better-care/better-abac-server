package care.better.abac.dto.content;

import care.better.core.TimeUtils;

import java.time.OffsetDateTime;
import java.util.Objects;

/**
 * @author Matic Ribic
 */
public class PlainPartyRelationDto implements PlainDto {
    private PlainPartyDto source;
    private String relationType;
    private PlainPartyDto target;
    private OffsetDateTime validUntil;

    public PlainPartyDto getSource() {
        return source;
    }

    public void setSource(PlainPartyDto source) {
        this.source = source;
    }

    public String getRelationType() {
        return relationType;
    }

    public void setRelationType(String relationType) {
        this.relationType = relationType;
    }

    public PlainPartyDto getTarget() {
        return target;
    }

    public void setTarget(PlainPartyDto target) {
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
        PlainPartyRelationDto that = (PlainPartyRelationDto)o;
        return Objects.equals(source, that.source) &&
                Objects.equals(relationType, that.relationType) &&
                Objects.equals(target, that.target) &&
                TimeUtils.equalOffsetDateTime(validUntil, that.validUntil);
    }

    @Override
    public int hashCode() {
        return Objects.hash(source, relationType, target, validUntil);
    }

    @Override
    public String toString() {
        return String.format("PlainPartyRelationDto{relationType='%s', source='%s', target='%s', validUntil='%s'}", relationType, source, target, validUntil);
    }
}
