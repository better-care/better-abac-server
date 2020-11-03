package care.better.abac.policy.definition;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

/**
 * @author Bostjan Lah
 */
public class PolicyFunctionConstantParameter implements PolicyFunctionParameter {
    @JsonProperty
    private final String value;

    @JsonCreator
    public PolicyFunctionConstantParameter(@JsonProperty("value") String value) {
        this.value = value;
    }

    @Override
    public Object resolve(Map<String, Object> context) {
        return value;
    }

    @Override
    public String toString() {
        return "const";
    }
}
