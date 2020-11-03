package care.better.abac.rest;

import care.better.abac.exception.PartyRelationInvalidTypesException;
import care.better.abac.exception.PolicyExecutionException;
import care.better.abac.exception.PolicyNotFoundException;
import care.better.abac.rest.client.ValidationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Bostjan Lah
 */
@RestController
@ControllerAdvice
public class AbacExceptionHandler {
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(PolicyExecutionException.class)
    public ExceptionData handleExecutionException(PolicyExecutionException exception) {
        return new ExceptionData(exception);
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(PolicyNotFoundException.class)
    public ExceptionData handleNotFoundException(PolicyNotFoundException exception) {
        return new ExceptionData(exception);
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(EmptyResultDataAccessException.class)
    public ExceptionData handleEmptyResultDataAccessException(EmptyResultDataAccessException exception) {
        return new ExceptionData(exception);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(value = PartyRelationInvalidTypesException.class)
    public ExceptionData exceptionHandler(PartyRelationInvalidTypesException exception) {
        return new ExceptionData(exception);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ValidationException.class)
    public ExceptionData handleValidationException(ValidationException exception) {
        return new ExceptionData(exception);
    }
}
