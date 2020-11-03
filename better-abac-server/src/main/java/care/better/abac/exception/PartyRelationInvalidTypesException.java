package care.better.abac.exception;

/**
 * @author Bostjan Lah
 */
public class PartyRelationInvalidTypesException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public PartyRelationInvalidTypesException(String message) {
        super(message);
    }
}
