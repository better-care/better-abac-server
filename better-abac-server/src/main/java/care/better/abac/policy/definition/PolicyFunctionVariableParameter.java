package care.better.abac.policy.definition;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import care.better.abac.exception.PolicyExecutionException;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author Bostjan Lah
 */
public class PolicyFunctionVariableParameter implements PolicyFunctionParameter {
    private static final Pattern DOT_PATTERN = Pattern.compile(".", Pattern.LITERAL);

    private final List<String> segments;
    private final String lastSegment;
    @JsonProperty
    private final String var;

    @JsonCreator
    public PolicyFunctionVariableParameter(@JsonProperty("var") String var) {
        segments = DOT_PATTERN.splitAsStream(var).skip(1L).collect(Collectors.toList());
        lastSegment = segments.remove(segments.size() - 1);
        this.var = var;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object resolve(Map<String, Object> context) {
        Map<String, Object> map = context;
        String lastExecuted = null;
        for (String segment : segments) {
            Object value = map.get(segment);
            lastExecuted = segment;
            if (value instanceof Map) {
                map = (Map<String, Object>)value;
            } else {
                map = null;
                break;
            }
        }

        if (map == null) {
            throw new PolicyExecutionException("Unable to resolve value for '" + var + "', last successful segment = '" + lastExecuted + "'!");
        }
        return map.get(lastSegment);
    }

    @Override
    public String toString() {
        return var;
    }
}
