package care.better.abac.policy.execute;

import care.better.abac.policy.definition.PolicyFunctionParameter;
import care.better.abac.policy.execute.Executable.Type;
import care.better.abac.policy.execute.evaluation.EvaluationExpression;
import lombok.NonNull;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * @author Andrej Dolenc
 */
public class ExecutableConversion extends BaseExecutable {

    protected ExecutableConversion() {
        super(Executable.CONVERSION_TYPES);
    }

    public final EvaluationExpression convert(
            @NonNull PolicyExecutionContext context,
            @NonNull EvaluationExpression expression,
            @NonNull PolicyFunctionParameter[] parameters) {
        return execute(context, ArrayUtils.addAll(new PolicyFunctionParameter[]{ctx -> expression}, parameters), Type.CONVERT);
    }

    @Override
    public String getName() {
        return StringUtils.removeEnd(StringUtils.uncapitalize(getClass().getSimpleName()), "Conversion");
    }
}
