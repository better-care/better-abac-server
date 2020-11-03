package care.better.abac.policy.definition;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import care.better.abac.policy.execute.PolicyExecutionContext;
import care.better.abac.policy.execute.evaluation.BooleanEvaluationExpression;
import care.better.abac.policy.execute.evaluation.BooleanOperation;
import care.better.abac.policy.execute.evaluation.BooleanOperationEvaluationExpression;
import care.better.abac.policy.execute.evaluation.EvaluationExpression;
import lombok.Getter;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * @author Andrej Dolenc
 */
public class DecisionOperation implements PolicyRule {
    public enum Quantifier {ALL_OF, ANY_OF, NONE_OF}

    @Getter
    private final Quantifier quantifier;

    @Getter
    private final List<PolicyRule> operations;

    @JsonCreator
    public DecisionOperation(
            @NonNull @JsonProperty("quantifier") Quantifier quantifier,
            @NonNull @JsonProperty("operations") List<PolicyRule> operations) {
        this.quantifier = quantifier;
        this.operations = Collections.unmodifiableList(new ArrayList<>(operations));
    }

    @Override
    public EvaluationExpression evaluate(@NonNull PolicyExecutionContext context) {
        return createEvaluationExpression(policyRule -> policyRule.evaluate(context));
    }

    @Override
    public EvaluationExpression query(@NonNull PolicyExecutionContext context) {
        return createEvaluationExpression(policyRule -> policyRule.query(context));
    }

    private EvaluationExpression createEvaluationExpression(Function<PolicyRule, EvaluationExpression> policyFunction) {
        switch (quantifier) {
            case ALL_OF:
                return reduce(policyFunction, BooleanOperation.AND);
            case ANY_OF:
                return reduce(policyFunction, BooleanOperation.OR);
            case NONE_OF:
                EvaluationExpression expression = reduce(policyFunction, BooleanOperation.OR);
                if (expression instanceof BooleanEvaluationExpression) {
                    return new BooleanEvaluationExpression(!((BooleanEvaluationExpression)expression).getBooleanValue());
                } else {
                    return new BooleanOperationEvaluationExpression(BooleanOperation.NOT, expression, null);
                }
            default:
                throw new IllegalStateException("Unexpected quantifier: " + quantifier);
        }
    }

    private EvaluationExpression reduce(Function<PolicyRule, EvaluationExpression> policyFunction, BooleanOperation operation) {
        List<EvaluationExpression> expressions = new ArrayList<>();

        BooleanEvaluationExpression terminalExpression = operations.stream()
                .map(policyFunction)
                .map(e -> addAndGroup(e, operation, expressions))
                .filter(e -> isTerminalBooleanExpression(e, operation))
                .map(e -> (BooleanEvaluationExpression)e)
                .findAny()
                .orElse(null);
        if (terminalExpression != null) {
            return terminalExpression;
        } else if (expressions.size() > 1) {
            return expressions.stream().reduce(new BooleanOperationEvaluationExpression(operation), this::accumulate,
                                               (e1, e2) -> new BooleanOperationEvaluationExpression(operation, e1, e2));
        } else {
            return expressions.get(0);
        }
    }

    private EvaluationExpression addAndGroup(EvaluationExpression expression, BooleanOperation operation, List<EvaluationExpression> expressions) {
        EvaluationExpression evaluationExpression = expression;
        EvaluationExpression groupableExpression;
        while ((groupableExpression = findGroupable(evaluationExpression, operation, expressions)) != null) {
            expressions.remove(groupableExpression);
            evaluationExpression = groupableExpression.group(evaluationExpression, operation);
        }
        expressions.add(evaluationExpression);
        return evaluationExpression;
    }

    private EvaluationExpression findGroupable(EvaluationExpression expression, BooleanOperation operation, List<EvaluationExpression> expressions) {
        return expressions.stream().filter(e -> e.isGroupable(expression, operation)).findAny().orElse(null);
    }

    private boolean isTerminalBooleanExpression(EvaluationExpression expression, BooleanOperation operation) {
        if (expression instanceof BooleanEvaluationExpression) {
            boolean value = ((BooleanEvaluationExpression)expression).getBooleanValue();
            return (BooleanOperation.AND == operation && !value) || (BooleanOperation.OR == operation && value);
        } else {
            return false;
        }
    }

    private BooleanOperationEvaluationExpression accumulate(BooleanOperationEvaluationExpression operation, EvaluationExpression expression) {
        if (operation.getLeftChild() == null) {
            operation.setLeftChild(expression);
            return operation;
        } else if (operation.getRightChild() == null) {
            operation.setRightChild(expression);
            return operation;
        } else {
            BooleanOperationEvaluationExpression booleanOp = new BooleanOperationEvaluationExpression(operation.getBooleanOperation());
            booleanOp.setRightChild(operation);
            booleanOp.setLeftChild(expression);
            return booleanOp;
        }
    }
}
