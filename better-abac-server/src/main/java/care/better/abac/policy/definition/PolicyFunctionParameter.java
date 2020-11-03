package care.better.abac.policy.definition;

import java.util.Map;

/**
 * @author Bostjan Lah
 */
public interface PolicyFunctionParameter {
    Object resolve(Map<String, Object> context);
}
