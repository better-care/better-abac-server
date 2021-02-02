package care.better.abac.policy.execute.function;

import care.better.abac.policy.execute.Executable;
import care.better.abac.policy.execute.ExecutableFunction;
import care.better.abac.policy.execute.evaluation.BooleanEvaluationExpression;
import care.better.abac.policy.execute.evaluation.EvaluationExpression;
import com.google.common.collect.Sets;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static care.better.abac.policy.execute.function.Functions.convertIds;

/**
 * @author Andrej Dolenc
 */
@Component
public class HasContextVarFunction extends ExecutableFunction {
    @Executable(type = Executable.Type.EVALUATE)
    public EvaluationExpression hasContextVarEvaluate(Object contextValue, Object... values) {
        Set<String> ctxValue = new HashSet<>(convertIds(contextValue));
        Set<String> otherValue = Optional.ofNullable(values).map(v -> Arrays.stream(v).map(Object::toString).collect(Collectors.toSet())).orElse(Collections.emptySet());
        return new BooleanEvaluationExpression((otherValue.isEmpty() && !ctxValue.isEmpty()) || !Sets.intersection(ctxValue, otherValue).isEmpty());
    }

    @Executable(type = Executable.Type.QUERY)
    public EvaluationExpression hasContextVarQuery(Object contextValue, Object... values) {
        return hasContextVarEvaluate(contextValue, values);
    }
}
