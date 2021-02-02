package care.better.abac.policy.execute;

import com.google.common.base.Preconditions;
import care.better.abac.exception.PolicyExecutionException;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Bostjan Lah
 */

public class PolicyHelper {

    private final Map<String, ExecutableFunction> executableFunctions;
    private final Map<String, ExecutableConversion> executableConversions;

    public PolicyHelper(@NonNull List<ExecutableFunction> executableFunctions, @NonNull List<ExecutableConversion> executableConversions) {
        this.executableFunctions = executableFunctions.stream().collect(Collectors.toMap(ExecutableFunction::getName, Function.identity()));
        this.executableConversions = executableConversions.stream().collect(Collectors.toMap(ExecutableConversion::getName, Function.identity()));
    }

    public ExecutableFunction findExecutableFunction(@NonNull String functionName) {
        Preconditions.checkArgument(!StringUtils.isBlank(functionName), "Function name is blank!");
        return executableFunctions.computeIfAbsent(functionName, f -> {
            throw new PolicyExecutionException("Function '" + functionName + "'not found!");
        });
    }

    public ExecutableConversion findExecutableConversion(@NonNull String conversionName) {
        Preconditions.checkArgument(!StringUtils.isBlank(conversionName), "Conversion name is blank!");
        return executableConversions.computeIfAbsent(conversionName, f -> {
            throw new PolicyExecutionException("Conversion '" + conversionName + "'not found!");
        });
    }
}
