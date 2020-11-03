package care.better.abac.policy.execute;

import care.better.abac.audit.ExecutionErrorAuditEntry;
import care.better.abac.audit.FunctionExecutionAuditEntry;
import care.better.abac.exception.PolicyExecutionException;
import care.better.abac.policy.definition.PolicyFunctionParameter;
import care.better.abac.policy.execute.evaluation.EvaluationExpression;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Andrej Dolenc
 */
public abstract class BaseExecutable {

    @Getter(AccessLevel.PROTECTED)
    private final Map<Executable.Type, Method> methods;

    protected BaseExecutable(@NonNull EnumSet<Executable.Type> types) {
        methods = Arrays.stream(getClass().getDeclaredMethods())
                .filter(method -> method.isAnnotationPresent(Executable.class))
                .peek(ReflectionUtils::makeAccessible)
                .collect(Collectors.toMap(method -> method.getAnnotation(Executable.class).type(), Function.identity()));
        if (methods.keySet().size() != types.size()) {
            throw new IllegalArgumentException(
                    "Executable " + getClass().getName() + " must have @Executable annotation for each type of: " + types.stream().map(Enum::name).collect(
                            Collectors.joining(",")));
        }
    }

    protected EvaluationExpression execute(@NonNull PolicyExecutionContext context, @NonNull PolicyFunctionParameter[] parameters, Executable.Type type) {
        try {
            Method method = getMethods().get(type);
            Object[] parameterValues = getParameterValues(context, method, parameters);
            EvaluationExpression returnValue = (EvaluationExpression)method.invoke(this, parameterValues);
            audit(context, parameters, parameterValues, returnValue);
            return returnValue;
        } catch (IllegalAccessException | InvocationTargetException | PolicyExecutionException e) {
            String message = "Unable to execute " + type.name() + " function on: '" + getClass().getName() + "'! [" + e.getMessage() + ']';
            context.getExecutionLog().add(new ExecutionErrorAuditEntry(message));
            throw new PolicyExecutionException(message, e);
        }
    }

    protected <R> void audit(PolicyExecutionContext context, PolicyFunctionParameter[] parameters, Object[] parameterValues, R returnValue) {
        FunctionExecutionAuditEntry<R> auditEntry = new FunctionExecutionAuditEntry<>(getName(), returnValue, parameters, parameterValues);
        context.getExecutionLog().add(auditEntry);
    }

    protected Object[] getParameterValues(PolicyExecutionContext policyExecutionContext, Method functionMethod, PolicyFunctionParameter[] parameters) {
        Object[] parameterValues = Arrays.stream(parameters).map(p -> p.resolve(policyExecutionContext.getContext())).toArray();
        int parameterCount = functionMethod.getParameterCount();
        if (parameterCount < parameterValues.length) {
            parameterValues[parameterCount - 1] = Arrays.copyOfRange(parameterValues, parameterCount - 1, parameters.length);
        } else if (parameterCount == parameterValues.length && functionMethod.getParameterTypes()[parameterCount - 1] == Object[].class) {
            parameterValues[parameterCount - 1] = new Object[]{parameterValues[parameterCount - 1]};
        }
        return Arrays.copyOf(parameterValues, parameterCount);
    }

    public abstract String getName();
}
