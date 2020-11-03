package care.better.abac.policy.execute.function;

import care.better.abac.policy.execute.Executable;
import care.better.abac.policy.execute.ExecutableFunction;
import care.better.abac.policy.execute.evaluation.EvaluationExpression;
import care.better.abac.policy.execute.evaluation.Tag;
import care.better.abac.policy.execute.evaluation.TagSetEvaluationExpression;
import org.springframework.stereotype.Component;

import java.util.Collections;

/**
 * @author Andrej Dolenc
 */
@Component
public class HasTagFunction extends ExecutableFunction {

    @Executable(type = Executable.Type.EVALUATE)
    public EvaluationExpression hasTagEvaluate(String namespace, String value) {
        return new TagSetEvaluationExpression(Collections.singleton(new Tag(namespace, value)));
    }

    @Executable(type = Executable.Type.QUERY)
    public EvaluationExpression hasTagQuery(String namespace, String value) {
        return hasTagEvaluate(namespace, value);
    }
}
