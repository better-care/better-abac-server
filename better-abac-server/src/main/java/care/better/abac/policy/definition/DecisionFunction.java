package care.better.abac.policy.definition;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import care.better.abac.audit.ExecutionErrorAuditEntry;
import care.better.abac.exception.PolicyExecutionException;
import care.better.abac.policy.execute.ExecutableFunction;
import care.better.abac.policy.execute.PolicyExecutionContext;
import care.better.abac.policy.execute.evaluation.EvaluationExpression;
import lombok.Getter;
import lombok.NonNull;

/**
 * @author Bostjan Lah
 */
public class DecisionFunction implements PolicyRule {

    @Getter
    private final String functionName;

    @Getter
    private final PolicyFunctionParameter[] parameters;

    @JsonCreator
    public DecisionFunction(
            @NonNull @JsonProperty("functionName") String functionName,
            @NonNull @JsonProperty("parameters") PolicyFunctionParameter[] parameters) {
        this.functionName = functionName;
        this.parameters = parameters.clone();
    }

    @Override
    public EvaluationExpression evaluate(@NonNull PolicyExecutionContext policyExecutionContext) {
        try {
            ExecutableFunction function = policyExecutionContext.getPolicyHelper().findExecutableFunction(functionName);
            return function.evaluate(policyExecutionContext, parameters);
        } catch (PolicyExecutionException e) {
            String message = "Unable to evaluate function: '" + functionName + "'! [" + e.getMessage() + ']';
            policyExecutionContext.getExecutionLog().add(new ExecutionErrorAuditEntry(message));
            throw new PolicyExecutionException(message, e);
        }
    }

    @Override
    public EvaluationExpression query(@NonNull PolicyExecutionContext policyExecutionContext) {
        try {
            ExecutableFunction function = policyExecutionContext.getPolicyHelper().findExecutableFunction(functionName);
            return function.query(policyExecutionContext, parameters);
        } catch (PolicyExecutionException e) {
            String message = "Unable to query function: '" + functionName + "'! [" + e.getMessage() + ']';
            policyExecutionContext.getExecutionLog().add(new ExecutionErrorAuditEntry(message));
            throw new PolicyExecutionException(message, e);
        }
    }
}
