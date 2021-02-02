package care.better.abac.policy.definition;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import care.better.abac.audit.ExecutionErrorAuditEntry;
import care.better.abac.exception.PolicyExecutionException;
import care.better.abac.policy.execute.ExecutableConversion;
import care.better.abac.policy.execute.PolicyExecutionContext;
import care.better.abac.policy.execute.evaluation.EvaluationExpression;
import lombok.Getter;
import lombok.NonNull;

/**
 * @author Andrej Dolenc
 */
public class DecisionConversion implements PolicyRule {

    @Getter
    private final String conversionName;

    @Getter
    private final PolicyFunctionParameter[] parameters;

    @Getter
    private final PolicyRule rule;

    @JsonCreator
    public DecisionConversion(
            @NonNull @JsonProperty("conversionName") String conversionName,
            @NonNull @JsonProperty("rule") PolicyRule rule,
            @NonNull @JsonProperty("parameters") PolicyFunctionParameter[] parameters) {
        this.conversionName = conversionName;
        this.rule = rule;
        this.parameters = parameters.clone();
    }

    @Override
    public EvaluationExpression evaluate(@NonNull PolicyExecutionContext policyExecutionContext) {
        try {
            ExecutableConversion conversion = policyExecutionContext.getPolicyHelper().findExecutableConversion(conversionName);
            return conversion.convert(policyExecutionContext, rule.evaluate(policyExecutionContext), parameters);
        } catch (PolicyExecutionException e) {
            String message = "Unable to convert: '" + conversionName + "'! [" + e.getMessage() + ']';
            policyExecutionContext.getExecutionLog().add(new ExecutionErrorAuditEntry(message));
            throw new PolicyExecutionException(message, e);
        }
    }

    @Override
    public EvaluationExpression query(@NonNull PolicyExecutionContext policyExecutionContext) {
        try {
            ExecutableConversion conversion = policyExecutionContext.getPolicyHelper().findExecutableConversion(conversionName);
            return conversion.convert(policyExecutionContext, rule.query(policyExecutionContext), parameters);
        } catch (PolicyExecutionException e) {
            String message = "Unable to convert: '" + conversionName + "'! [" + e.getMessage() + ']';
            policyExecutionContext.getExecutionLog().add(new ExecutionErrorAuditEntry(message));
            throw new PolicyExecutionException(message, e);
        }
    }
}
