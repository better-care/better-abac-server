package care.better.abac.policy.execute.evaluation;

import lombok.NonNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Andrej Dolenc
 */
public final class EvaluationContext {
    public static final String USER_KEY = "user";
    public static final String QUERY_VALUE = "?";

    private Map<String, Object> context;

    public EvaluationContext() {
        context = new HashMap<>();
    }

    public EvaluationContext(@NonNull Map<String, Object> context) {
        setContext(context);
    }

    public Map<String, Object> getContext() {
        return Collections.unmodifiableMap(new HashMap<>(context));
    }

    public void setContext(@NonNull Map<String, Object> context) {
        this.context = new HashMap<>(context);
    }

    public void setContextValue(@NonNull String key, @NonNull Object value) {
        context.put(key, value);
    }

    public void setContextQuery(@NonNull String key) {
        context.put(key, QUERY_VALUE);
    }

    public boolean hasQuery() {
        return context.values().stream().anyMatch(QUERY_VALUE::equals);
    }

    public boolean isQueryValue(@NonNull String key) {
        return QUERY_VALUE.equals(context.get(key));
    }
}
