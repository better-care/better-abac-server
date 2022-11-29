package care.better.abac.dto.content;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * @author Matic Ribic
 */
public class PlainPartyDto implements PlainDto {
    private String type;
    private Set<String> externalIds = new HashSet<>();

    public PlainPartyDto() {
    }

    public PlainPartyDto(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Set<String> getExternalIds() {
        return externalIds;
    }

    public void setExternalIds(Set<String> externalIds) {
        this.externalIds = externalIds;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PlainPartyDto)) {
            return false;
        }
        PlainPartyDto that = (PlainPartyDto)o;
        return Objects.equals(type, that.type) && Objects.equals(externalIds, that.externalIds);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, externalIds);
    }

    @Override
    public String toString() {
        return String.format("PlainPartyDto{type='%s', externalIds='%s'}", type, externalIds);
    }
}
