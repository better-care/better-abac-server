package care.better.abac.policy.definition;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import care.better.abac.policy.execute.PolicyExecutionContext;
import care.better.abac.policy.execute.evaluation.EvaluationExpression;
import lombok.Getter;
import lombok.NonNull;

/**
 * @author Bostjan Lah
 */
public class DecisionPolicyRule implements PolicyRule {

    @Getter
    private final PolicyRule operation;

    @JsonCreator
    public DecisionPolicyRule(@NonNull @JsonProperty("operation") PolicyRule operation) {
        this.operation = operation;
    }

    @Override
    public EvaluationExpression evaluate(@NonNull PolicyExecutionContext context) {
        return TerminalPolicyRule.fromExpression(operation.evaluate(context)).evaluate(context);
    }

    @Override
    public EvaluationExpression query(PolicyExecutionContext context) {
        return TerminalPolicyRule.fromExpression(operation.query(context)).query(context);
    }
}
