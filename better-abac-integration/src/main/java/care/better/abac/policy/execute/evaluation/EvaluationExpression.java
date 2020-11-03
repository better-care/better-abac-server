package care.better.abac.policy.execute.evaluation;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Andrej Dolenc
 */
@SuppressWarnings("ClassReferencesSubclass")
@JsonTypeInfo(use = Id.NAME, property = "_type")
@JsonSubTypes({
                      @Type(value = BooleanEvaluationExpression.class, name = "BooleanEvaluation"),
                      @Type(value = BooleanOperationEvaluationExpression.class, name = "BooleanOperation"),
                      @Type(value = ResultSetEvaluationExpression.class, name = "ResultSet"),
                      @Type(value = TagSetEvaluationExpression.class, name = "TagSet")
              })
public abstract class EvaluationExpression implements BinaryTreeElement<EvaluationExpression, EvaluationExpression> {
    @Getter
    @Setter
    @JsonIgnore
    private EvaluationExpression parent;
    @Getter
    private EvaluationExpression leftChild;
    @Getter
    private EvaluationExpression rightChild;

    @Override
    public void setLeftChild(EvaluationExpression leftChild) {
        this.leftChild = leftChild;
        if (leftChild != null) {
            leftChild.parent = this;
        }
    }

    @Override
    public void setRightChild(EvaluationExpression rightChild) {
        this.rightChild = rightChild;
        if (rightChild != null) {
            rightChild.parent = this;
        }
    }

    @JsonIgnore
    public boolean isGroupable(EvaluationExpression expression, BooleanOperation operation) {
        if (expression instanceof BooleanEvaluationExpression) {
            return expression.isGroupable(this, operation);
        } else {
            return false;
        }
    }

    @JsonIgnore
    public EvaluationExpression group(EvaluationExpression expression, BooleanOperation operation) {
        if (expression.isGroupable(this, operation)) {
            return expression.group(this, operation);
        } else {
            throw new UnsupportedOperationException("Expressions can not be grouped together");
        }
    }
}
