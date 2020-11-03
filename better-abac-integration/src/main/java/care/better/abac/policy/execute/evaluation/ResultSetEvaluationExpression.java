package care.better.abac.policy.execute.evaluation;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NonNull;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Andrej Dolenc
 */
public class ResultSetEvaluationExpression extends EvaluationExpression {

    @Getter
    private final Set<String> externalIds;

    @JsonCreator
    public ResultSetEvaluationExpression(@NonNull @JsonProperty("externalIds") Set<String> externalIds) {
        this.externalIds = Collections.unmodifiableSet(new HashSet<>(externalIds));
    }

    @Override
    public boolean isGroupable(EvaluationExpression expression, BooleanOperation operation) {
        if (BooleanOperation.OR == operation || BooleanOperation.AND == operation) {
            return super.isGroupable(expression, operation) || expression instanceof ResultSetEvaluationExpression;
        }
        return false;
    }

    @Override
    public EvaluationExpression group(EvaluationExpression expression, BooleanOperation operation) {
        if (isGroupable(expression, operation)) {
            if (expression instanceof BooleanEvaluationExpression) {
                return expression.group(this, operation);
            } else if (expression instanceof ResultSetEvaluationExpression) {
                Set<String> ids = new HashSet<>(((ResultSetEvaluationExpression)expression).getExternalIds());
                switch (operation) {
                    case AND:
                        ids.retainAll(externalIds);
                        return ids.isEmpty() ? new BooleanEvaluationExpression(false) : new ResultSetEvaluationExpression(ids);
                    case OR:
                        ids.addAll(externalIds);
                        return new ResultSetEvaluationExpression(ids);
                }
            }
        }
        throw new UnsupportedOperationException("Expressions can not be grouped together");
    }

    public static EvaluationExpression create(Set<String> externalIds) {
        if (externalIds == null) {
            return new BooleanEvaluationExpression(true);
        } else if (externalIds.isEmpty()) {
            return new BooleanEvaluationExpression(false);
        } else {
            return new ResultSetEvaluationExpression(externalIds);
        }
    }
}
