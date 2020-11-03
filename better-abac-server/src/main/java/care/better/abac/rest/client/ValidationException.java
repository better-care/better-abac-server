package care.better.abac.rest.client;

import com.google.common.collect.ImmutableList;
import care.better.abac.dto.config.ExternalSystemValidationStatus;
import care.better.abac.dto.config.ValidationErrorDto;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Matic Ribic
 */
public final class ValidationException extends Exception {
    private static final long serialVersionUID = 2601762150456868278L;

    private final ExternalSystemValidationStatus status;
    private final List<ValidationErrorDto> errors;

    private ValidationException(ExternalSystemValidationStatus status, List<ValidationErrorDto> errors) {
        super(createMessage(status, errors));
        this.status = status;
        this.errors = ImmutableList.copyOf(errors);
    }

    public static ValidationException unknown() {
        return new ValidationException(ExternalSystemValidationStatus.UNKNOWN, Collections.emptyList());
    }

    public static ValidationException of(List<ValidationErrorDto> errors) {
        return new ValidationException(errors.isEmpty() ? ExternalSystemValidationStatus.VALID : ExternalSystemValidationStatus.INVALID, errors);
    }

    public ExternalSystemValidationStatus getStatus() {
        return status;
    }

    public List<ValidationErrorDto> getErrors() {
        return errors;
    }

    private static String createMessage(ExternalSystemValidationStatus status, List<ValidationErrorDto> errors) {
        String message = "Status: " + status;
        if (!errors.isEmpty()) {
            message += ". Errors: " + errors.stream().map(ValidationErrorDto::toString).collect(Collectors.joining(", "));
        }

        return message;
    }
}
