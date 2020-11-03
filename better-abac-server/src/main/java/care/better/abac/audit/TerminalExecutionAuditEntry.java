package care.better.abac.audit;

/**
 * @author Bostjan Lah
 */
public class TerminalExecutionAuditEntry implements ExecutionAuditEntry {
    private final String value;

    public TerminalExecutionAuditEntry(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
