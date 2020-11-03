package care.better.abac.dto;

/**
 * @author Bostjan Lah
 */
public class PolicyDto extends NamedDtoWithId {
    private String policy;

    public PolicyDto() {
    }

    public PolicyDto(Long id, String name) {
        super(id, name);
    }

    public String getPolicy() {
        return policy;
    }

    public void setPolicy(String policy) {
        this.policy = policy;
    }
}
