package care.better.abac.policy.execute.conversion;

import care.better.abac.policy.execute.Executable;
import care.better.abac.policy.execute.ExecutableConversion;
import care.better.abac.policy.execute.evaluation.BooleanEvaluationExpression;
import care.better.abac.policy.execute.evaluation.EvaluationExpression;
import care.better.abac.policy.execute.evaluation.ResultSetEvaluationExpression;
import care.better.abac.policy.execute.evaluation.ValueSetEvaluationExpression;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * @author Andrej Dolenc
 */
@Component
public class AsValueSetConversion extends ExecutableConversion {
    @Executable(type = Executable.Type.CONVERT)
    public EvaluationExpression asValueSetConversion(EvaluationExpression expression, String path) {
        if (expression instanceof BooleanEvaluationExpression || expression instanceof ValueSetEvaluationExpression) {
            return expression;
        }
        if (expression instanceof ResultSetEvaluationExpression) {
            Set<String> values = ((ResultSetEvaluationExpression)expression).getExternalIds();
            return new ValueSetEvaluationExpression(path, values);
        } else {
            throw new UnsupportedOperationException("Cannot convert to ValueSetEvaluationExpression from " + expression.getClass().getSimpleName());
        }
    }
}
