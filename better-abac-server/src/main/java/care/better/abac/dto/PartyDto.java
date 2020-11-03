package care.better.abac.dto;

import java.util.Set;

/**
 * @author Bostjan Lah
 */
public class PartyDto extends DtoWithId {
    private String type;
    private Set<String> externalIds;
    private String fullName;

    public PartyDto() {
    }

    public PartyDto(Long id, String type) {
        super(id);
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

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
}
