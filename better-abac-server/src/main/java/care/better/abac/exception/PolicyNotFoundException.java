package care.better.abac.exception;

/**
 * @author Bostjan Lah
 */
public class PolicyNotFoundException extends RuntimeException {
    public PolicyNotFoundException() {
    }

    public PolicyNotFoundException(String message) {
        super(message);
    }

    public PolicyNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public PolicyNotFoundException(Throwable cause) {
        super(cause);
    }

    public PolicyNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
