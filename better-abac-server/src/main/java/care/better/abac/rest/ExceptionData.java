package care.better.abac.rest;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Bostjan Lah
 */
public final class ExceptionData {
    private final String exceptionName;
    private final String errorMessage;

    @JsonCreator
    private ExceptionData(@JsonProperty("exceptionName") String exceptionName, @JsonProperty("errorMessage") String errorMessage) {
        this.exceptionName = exceptionName;
        this.errorMessage = errorMessage;
    }

    public ExceptionData(Exception exception) {
        this(exception.getClass().getSimpleName(), exception.getMessage());
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getExceptionName() {
        return exceptionName;
    }
}
