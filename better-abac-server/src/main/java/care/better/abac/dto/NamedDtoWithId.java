package care.better.abac.dto;

/**
 * @author Bostjan Lah
 */
public class NamedDtoWithId extends DtoWithId {
    private String name;

    public NamedDtoWithId() {
    }

    public NamedDtoWithId(Long id, String name) {
        super(id);
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
