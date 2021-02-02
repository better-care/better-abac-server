package care.better.abac.policy.execute.function;

import care.better.abac.policy.execute.Executable;
import care.better.abac.policy.execute.ExecutableFunction;
import care.better.abac.policy.execute.evaluation.EvaluationExpression;
import care.better.abac.policy.execute.evaluation.TagSetEvaluationExpression;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * @author Andrej Dolenc
 */
@Component
public class HasAnyTagFunction extends ExecutableFunction {

    @Executable(type = Executable.Type.EVALUATE)
    public EvaluationExpression hasAnyTagEvaluate(Object... tags) {
        return TagSetEvaluationExpression.create(Arrays.stream(tags).map(Object::toString).collect(Collectors.toSet()));
    }

    @Executable(type = Executable.Type.QUERY)
    public EvaluationExpression hasAnyTagQuery(Object... tags) {
        return hasAnyTagEvaluate(tags);
    }
}
