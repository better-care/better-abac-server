package care.better.abac.exception;

/**
 * @author Matic Ribic
 */
public class AppContentSyncException extends RuntimeException {
    private final String detailCode;
    private final String errorMessage;

    public static final AppContentSyncException EMPTY_BODY = new AppContentSyncException("SYNC-001", "Body is empty!");

    protected AppContentSyncException(String detailCode, String errorMessage) {
        this.detailCode = detailCode;
        this.errorMessage = errorMessage;
    }

    @Override
    public String getMessage() {
        return detailCode + ": " + errorMessage;
    }
}
