package care.better.abac.dto.config;

/**
 * @author Matic Ribic
 */
public class ExternalSystemDto extends AbstractExternalSystemDto {
    private String externalId;
    private String configHash;
    private ExternalSystemValidationStatus validationStatus;

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public String getConfigHash() {
        return configHash;
    }

    public void setConfigHash(String configHash) {
        this.configHash = configHash;
    }

    public ExternalSystemValidationStatus getValidationStatus() {
        return validationStatus;
    }

    public void setValidationStatus(ExternalSystemValidationStatus validationStatus) {
        this.validationStatus = validationStatus;
    }
}
