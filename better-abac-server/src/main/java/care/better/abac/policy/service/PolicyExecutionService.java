package care.better.abac.policy.service;

import care.better.abac.policy.execute.evaluation.EvaluationExpression;

import java.util.Map;

/**
 * @author Bostjan Lah
 */
public interface PolicyExecutionService {
    EvaluationExpression executeByName(String name, Map<String, Object> ctx);
    EvaluationExpression queryByName(String name, Map<String, Object> ctx);
    default void policyUpdated(String name)
    {
        policyUpdated(name, false);
    }
    void policyUpdated(String name, boolean refresh);
}
