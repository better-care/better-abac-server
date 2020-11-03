package care.better.abac.policy.definition;

import care.better.abac.policy.execute.PolicyExecutionContext;
import care.better.abac.policy.execute.evaluation.EvaluationExpression;

/**
 * @author Bostjan Lah
 */
public interface PolicyRule {
    EvaluationExpression evaluate(PolicyExecutionContext policyExecutionContext);
    EvaluationExpression query(PolicyExecutionContext policyExecutionContext);
}
