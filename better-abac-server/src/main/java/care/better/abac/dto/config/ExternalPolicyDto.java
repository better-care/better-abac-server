package care.better.abac.dto.config;

/**
 * @author Matic Ribic
 */
public class ExternalPolicyDto {
    private String externalId;
    private String name;
    private ExternalPolicyType type;
    private ExternalPolicyPhase phase;
    private String config;

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ExternalPolicyType getType() {
        return type;
    }

    public void setType(ExternalPolicyType type) {
        this.type = type;
    }

    public ExternalPolicyPhase getPhase() {
        return phase;
    }

    public void setPhase(ExternalPolicyPhase phase) {
        this.phase = phase;
    }

    public String getConfig() {
        return config;
    }

    public void setConfig(String config) {
        this.config = config;
    }
}
