package care.better.abac.dto;

/**
 * @author Gregor Berger
 */
public class ApplicationInfoDto {
    private String version;
    private String timeZone;
    private int offsetFromUtcInSeconds;

    public ApplicationInfoDto(String version, String timeZone, int offsetFromUtcInSeconds) {
        this.version = version;
        this.timeZone = timeZone;
        this.offsetFromUtcInSeconds = offsetFromUtcInSeconds;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    public int getOffsetFromUtcInSeconds() {
        return offsetFromUtcInSeconds;
    }

    public void setOffsetFromUtcInSeconds(int offsetFromUtcInSeconds) {
        this.offsetFromUtcInSeconds = offsetFromUtcInSeconds;
    }
}
