package care.better.abac.policy.definition;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import care.better.abac.audit.PolicyExecutionAuditor;
import care.better.abac.policy.execute.PolicyExecutionContext;
import care.better.abac.policy.execute.evaluation.EvaluationExpression;
import lombok.Getter;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.Callable;

/**
 * @author Bostjan Lah
 */
public class PolicyDefinition {
    private static final Logger log = LogManager.getLogger(PolicyDefinition.class.getName());

    @Getter
    private final PolicyRule policyRule;

    @JsonCreator
    public PolicyDefinition(@JsonProperty("policyRule") PolicyRule policyRule) {
        this.policyRule = policyRule;
    }

    public EvaluationExpression evaluate(String callId, PolicyExecutionContext policyExecutionContext, PolicyExecutionAuditor auditor) {
        return executeWithAuditing(() -> policyRule.evaluate(policyExecutionContext), callId, policyExecutionContext, auditor);
    }

    public EvaluationExpression query(String callId, PolicyExecutionContext policyExecutionContext, PolicyExecutionAuditor auditor) {
        return executeWithAuditing(() -> policyRule.query(policyExecutionContext), callId, policyExecutionContext, auditor);
    }

    private <V> V executeWithAuditing(Callable<V> call, String callId, PolicyExecutionContext policyExecutionContext, PolicyExecutionAuditor auditor) {
        try {
            V result = call.call();
            auditor.auditCall(callId, policyExecutionContext.getExecutionLog());
            return result;
        } catch (Exception e) {
            auditor.auditCall(callId, policyExecutionContext.getExecutionLog());
            log.error("Evaluation failed!", e);
            ExceptionUtils.rethrow(e);
        }
        return null;
    }
}
