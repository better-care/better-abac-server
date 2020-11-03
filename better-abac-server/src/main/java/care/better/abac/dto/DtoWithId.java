package care.better.abac.dto;

/**
 * @author Bostjan Lah
 */
public class DtoWithId {
    private Long id;

    public DtoWithId() {
    }

    public DtoWithId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
