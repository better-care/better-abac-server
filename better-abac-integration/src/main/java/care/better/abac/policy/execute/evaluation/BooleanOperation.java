package care.better.abac.policy.execute.evaluation;

import lombok.Getter;

/**
 * @author Andrej Dolenc
 */
public enum BooleanOperation {
    AND(2),
    OR(1),
    NOT(3);

    @Getter
    private final int precedence;

    BooleanOperation(int precedence) {
        this.precedence = precedence;
    }
}