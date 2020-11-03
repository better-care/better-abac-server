package care.better.abac.dto;

/**
 * @author Bostjan Lah
 */
public class PartyTypeDto extends NamedDtoWithId {
    public PartyTypeDto() {
    }

    public PartyTypeDto(Long id, String name) {
        super(id, name);
    }
}
