package care.better.abac.policy.execute;

import care.better.abac.audit.ExecutionAuditEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Bostjan Lah
 */
public class PolicyExecutionContext {
    private final List<ExecutionAuditEntry> executionLog = new ArrayList<>();
    private final Map<String, Object> context;
    private final PolicyHelper policyHelper;

    public PolicyExecutionContext(Map<String, Object> context, PolicyHelper policyHelper) {
        this.context = context;
        this.policyHelper = policyHelper;
    }

    public Map<String, Object> getContext() {
        return context;
    }

    public PolicyHelper getPolicyHelper() {
        return policyHelper;
    }

    public List<ExecutionAuditEntry> getExecutionLog() {
        return executionLog;
    }
}
