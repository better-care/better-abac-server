package care.better.abac.policy.definition;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import care.better.abac.audit.TerminalExecutionAuditEntry;
import care.better.abac.policy.execute.PolicyExecutionContext;
import care.better.abac.policy.execute.evaluation.BooleanEvaluationExpression;
import care.better.abac.policy.execute.evaluation.EvaluationExpression;
import lombok.Getter;
import lombok.NonNull;

/**
 * @author Bostjan Lah
 */
public class TerminalPolicyRule implements PolicyRule {
    public enum Outcome {ALLOW, DENY, CONDITIONAL}

    @Getter
    private final Outcome outcome;

    @Getter
    private final EvaluationExpression expression;

    @JsonCreator
    public TerminalPolicyRule(@NonNull @JsonProperty("outcome") Outcome outcome, @JsonProperty("expression") EvaluationExpression expression) {
        this.outcome = outcome;
        this.expression = expression;
    }

    @Override
    public EvaluationExpression evaluate(PolicyExecutionContext policyExecutionContext) {
        policyExecutionContext.getExecutionLog().add(new TerminalExecutionAuditEntry(outcome.toString()));
        return expression;
    }

    @Override
    public EvaluationExpression query(PolicyExecutionContext policyExecutionContext) {
        String result = expression.toString(); //TODO: implement serialization
        policyExecutionContext.getExecutionLog().add(new TerminalExecutionAuditEntry(outcome + ": " + result));
        return expression;
    }

    public static TerminalPolicyRule fromExpression(EvaluationExpression expression) {
        if (expression instanceof BooleanEvaluationExpression) {
            return ((BooleanEvaluationExpression)expression).getBooleanValue()
                    ? new TerminalPolicyRule(Outcome.ALLOW, expression)
                    : new TerminalPolicyRule(Outcome.DENY, expression);
        } else {
            return new TerminalPolicyRule(Outcome.CONDITIONAL, expression);
        }
    }
}
