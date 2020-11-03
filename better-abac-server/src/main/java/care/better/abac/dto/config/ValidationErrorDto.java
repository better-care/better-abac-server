package care.better.abac.dto.config;

/**
 * @author Matic Ribic
 */
public class ValidationErrorDto {
    private String externalId;
    private String message;

    public ValidationErrorDto() {
    }

    public ValidationErrorDto(String externalId, String message) {
        this.externalId = externalId;
        this.message = message;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return String.format("%s (externalId: %s)", message, externalId);
    }
}
