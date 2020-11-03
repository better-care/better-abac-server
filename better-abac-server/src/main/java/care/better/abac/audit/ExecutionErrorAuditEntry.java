
package care.better.abac.audit;

/**
 * @author Bostjan Lah
 */
public class ExecutionErrorAuditEntry implements ExecutionAuditEntry {
    private final String message;

    public ExecutionErrorAuditEntry(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
