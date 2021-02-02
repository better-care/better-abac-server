package care.better.abac.policy.execute.evaluation;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NonNull;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Conditions that requires that resource contains any of specified values inside resource path. Empty set means, that a path exists with any value.
 *
 * @author Andrej Dolenc
 */
public class ValueSetEvaluationExpression extends EvaluationExpression {

    @Getter
    private final String path;
    @Getter
    private final Set<String> values;

    @JsonCreator
    public ValueSetEvaluationExpression(@NonNull @JsonProperty("path") String path, @NonNull @JsonProperty("values") Set<String> values) {
        this.path = path;
        this.values = Collections.unmodifiableSet(new HashSet<>(values));
    }

    @Override
    public boolean isGroupable(EvaluationExpression expression, BooleanOperation operation) {
        if (BooleanOperation.OR == operation || BooleanOperation.AND == operation) {
            return super.isGroupable(expression, operation) ||
                   (expression instanceof ValueSetEvaluationExpression && path.equals(((ValueSetEvaluationExpression)expression).getPath()));
        }
        return false;
    }

    @Override
    public EvaluationExpression group(EvaluationExpression expression, BooleanOperation operation) {
        if (isGroupable(expression, operation)) {
            if (expression instanceof BooleanEvaluationExpression) {
                return expression.group(this, operation);
            } else if (expression instanceof ValueSetEvaluationExpression && path.equals(((ValueSetEvaluationExpression)expression).getPath())) {
                Set<String> expressionValues = new HashSet<>(((ValueSetEvaluationExpression)expression).getValues());
                switch (operation) {
                    case AND:
                        if (expressionValues.isEmpty()) {
                            return this;
                        } else {
                            expressionValues.retainAll(values);
                            return expressionValues.isEmpty()
                                    ? new BooleanEvaluationExpression(false)
                                    : new ValueSetEvaluationExpression(path, expressionValues);
                        }
                    case OR:
                        if (expressionValues.isEmpty()) {
                            return expression;
                        } else {
                            expressionValues.addAll(values);
                            return new ValueSetEvaluationExpression(path, expressionValues);
                        }
                }
            }
        }
        throw new UnsupportedOperationException("Expressions can not be grouped together");
    }
}
