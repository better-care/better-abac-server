package care.better.abac.audit;

import java.util.List;

/**
 * @author Bostjan Lah
 */
public interface PolicyExecutionAuditor {
    void auditCall(String callId, List<ExecutionAuditEntry> auditLog);
}
