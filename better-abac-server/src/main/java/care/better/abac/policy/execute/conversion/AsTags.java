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
public class AsTags extends ExecutableConversion {

    @Executable(type = Executable.Type.CONVERT)
    public EvaluationExpression asTagsConversion(EvaluationExpression expression, String tag) {
        if (expression instanceof BooleanEvaluationExpression || expression instanceof TagSetEvaluationExpression) {
            return expression;
        }
        if (expression instanceof ResultSetEvaluationExpression) {
            Set<Tag> tags = ((ResultSetEvaluationExpression)expression).getExternalIds()
                    .stream()
                    .map(id -> new Tag(tag, id))
                    .collect(Collectors.toSet());
            return new TagSetEvaluationExpression(tags);
        } else {
            throw new UnsupportedOperationException("Cannot convert to TagSetEvaluationExpression from " + expression.getClass().getSimpleName());
        }
    }
}
