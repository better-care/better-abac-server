package care.better.abac.policy.execute.evaluation;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NonNull;

/**
 * @author Andrej Dolenc
 */
public final class BooleanOperationEvaluationExpression extends EvaluationExpression {
    @Getter
    private final BooleanOperation booleanOperation;

    public BooleanOperationEvaluationExpression(@NonNull BooleanOperation booleanOperation) {
        this(booleanOperation, null, null);
    }

    @JsonCreator
    public BooleanOperationEvaluationExpression(
            @NonNull @JsonProperty("booleanOperation") BooleanOperation booleanOperation,
            @JsonProperty("left") EvaluationExpression left,
            @JsonProperty("right") EvaluationExpression right) {
        this.booleanOperation = booleanOperation;
        setLeftChild(left);
        setRightChild(right);
    }
}
