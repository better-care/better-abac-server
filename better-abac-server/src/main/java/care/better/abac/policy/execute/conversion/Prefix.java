package care.better.abac.policy.execute.conversion;

import care.better.abac.policy.execute.Executable;
import care.better.abac.policy.execute.ExecutableConversion;
import care.better.abac.policy.execute.evaluation.BooleanEvaluationExpression;
import care.better.abac.policy.execute.evaluation.EvaluationExpression;
import care.better.abac.policy.execute.evaluation.ResultSetEvaluationExpression;
import care.better.abac.policy.execute.evaluation.Tag;
import care.better.abac.policy.execute.evaluation.TagSetEvaluationExpression;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Andrej Dolenc
 */
@Component
public class Prefix extends ExecutableConversion {

    @Executable(type = Executable.Type.CONVERT)
    public EvaluationExpression prefixConversion(EvaluationExpression expression, String prefix) {
        if (expression instanceof BooleanEvaluationExpression) {
            return expression;
        }
        if (expression instanceof TagSetEvaluationExpression) {
            Set<Tag> tags = ((TagSetEvaluationExpression)expression).getTags()
                    .stream()
                    .map(t -> new Tag(t.getTag(), prefix + t.getValue()))
                    .collect(Collectors.toSet());
            return new TagSetEvaluationExpression(tags);
        }
        if (expression instanceof ResultSetEvaluationExpression) {
            Set<String> ids = ((ResultSetEvaluationExpression)expression).getExternalIds()
                    .stream()
                    .map(id -> prefix + id)
                    .collect(Collectors.toSet());
            return new ResultSetEvaluationExpression(ids);
        } else {
            throw new UnsupportedOperationException("Cannot convert to TagSetEvaluationExpression from " + expression.getClass().getSimpleName());
        }
    }
}
