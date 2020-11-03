package care.better.abac.policy.execute;

import care.better.abac.policy.definition.PolicyFunctionParameter;
import care.better.abac.policy.execute.Executable.Type;
import care.better.abac.policy.execute.evaluation.EvaluationExpression;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;

/**
 * @author Andrej Dolenc
 */
public abstract class ExecutableFunction extends BaseExecutable {

    protected ExecutableFunction() {
        super(Executable.FUNCTION_TYPES);
    }

    public final EvaluationExpression evaluate(@NonNull PolicyExecutionContext context, @NonNull PolicyFunctionParameter[] parameters) {
        return execute(context, parameters, Type.EVALUATE);
    }

    public final EvaluationExpression query(@NonNull PolicyExecutionContext context, @NonNull PolicyFunctionParameter[] parameters) {
        return execute(context, parameters, Type.QUERY);
    }

    @Override
    public String getName() {
        return StringUtils.removeEnd(StringUtils.uncapitalize(getClass().getSimpleName()), "Function");
    }
}
