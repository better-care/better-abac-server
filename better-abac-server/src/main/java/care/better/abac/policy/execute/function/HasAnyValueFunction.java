package care.better.abac.policy.execute.function;

import care.better.abac.policy.execute.Executable;
import care.better.abac.policy.execute.ExecutableFunction;
import care.better.abac.policy.execute.evaluation.EvaluationExpression;
import care.better.abac.policy.execute.evaluation.ValueSetEvaluationExpression;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Andrej Dolenc
 */
@Component
public class HasAnyValueFunction extends ExecutableFunction {
    @Executable(type = Executable.Type.EVALUATE)
    public EvaluationExpression hasAnyValueEvaluate(String path, Object... values) {
        return new ValueSetEvaluationExpression(path, Optional.ofNullable(values)
                .map(v -> Arrays.stream(v)
                        .filter(Objects::nonNull)
                        .map(Object::toString)
                        .collect(Collectors.toSet()))
                .orElse(Collections.emptySet()));
    }

    @Executable(type = Executable.Type.QUERY)
    public EvaluationExpression hasAnyValueQuery(String path, Object... values) {
        return hasAnyValueEvaluate(path, values);
    }
}
