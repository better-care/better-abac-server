package care.better.abac.policy.execute.evaluation;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Andrej Dolenc
 */
public class BooleanEvaluationExpression extends EvaluationExpression {

    private final boolean booleanValue;

    @JsonCreator
    public BooleanEvaluationExpression(@JsonProperty("booleanValue") boolean booleanValue) {
        this.booleanValue = booleanValue;
    }

    public boolean getBooleanValue() {
        return booleanValue;
    }

    @Override
    public boolean isGroupable(EvaluationExpression expression, BooleanOperation operation) {
        return BooleanOperation.OR == operation || BooleanOperation.AND == operation;
    }

    @Override
    public EvaluationExpression group(EvaluationExpression expression, BooleanOperation operation) {
        switch (operation) {
            case AND:
                return booleanValue ? expression : new BooleanEvaluationExpression(false);
            case OR:
                return booleanValue ? new BooleanEvaluationExpression(true) : expression;
            default:
                throw new UnsupportedOperationException("Expressions can not be grouped together");
        }
    }
}
