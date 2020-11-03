package care.better.abac.audit;

import care.better.abac.policy.definition.PolicyFunctionParameter;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author Bostjan Lah
 */
public class FunctionExecutionAuditEntry<R> implements ExecutionAuditEntry {
    @Getter
    private final String functionName;
    @Getter
    private final List<Parameter> parameters;
    @Getter
    private final R result;

    public FunctionExecutionAuditEntry(
            @NonNull String functionName,
            R result,
            @NonNull PolicyFunctionParameter[] parametersNames,
            @NonNull Object[] parameterValues) {
        this.functionName = functionName;
        this.result = result;
        parameters = IntStream.range(0, parametersNames.length)
                .mapToObj(i -> new Parameter(i, parametersNames[i].toString(), extractParameterValue(parameterValues, i)))
                .collect(Collectors.toList());
    }

    private Object extractParameterValue(Object[] parameterValues, int index) {
        return index < parameterValues.length ? parameterValues[index] : null;
    }

    @EqualsAndHashCode
    public static final class Parameter {
        @Getter
        private final int index;
        @Getter
        private final String name;
        @Getter
        private final Object value;

        public Parameter(int index, String name, Object value) {
            this.index = index;
            this.name = name;
            this.value = value;
        }
    }
}
