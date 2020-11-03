package care.better.abac.exception;

/**
 * @author Bostjan Lah
 */
public class PolicyExecutionException extends RuntimeException {
    public PolicyExecutionException() {
    }

    public PolicyExecutionException(String message) {
        super(message);
    }

    public PolicyExecutionException(String message, Throwable cause) {
        super(message, cause);
    }

    public PolicyExecutionException(Throwable cause) {
        super(cause);
    }

    public PolicyExecutionException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
