package care.better.abac.policy.execute.evaluation;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NonNull;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Andrej Dolenc
 */
public class TagSetEvaluationExpression extends EvaluationExpression {

    @Getter
    private final Set<Tag> tags;

    @JsonCreator
    public TagSetEvaluationExpression(@NonNull @JsonProperty("tags") Set<Tag> tags) {
        this.tags = Collections.unmodifiableSet(new HashSet<>(tags));
    }

    @Override
    public boolean isGroupable(EvaluationExpression expression, BooleanOperation operation) {
        if (BooleanOperation.OR == operation || BooleanOperation.AND == operation) {
            return super.isGroupable(expression, operation) || expression instanceof TagSetEvaluationExpression;
        }
        return false;
    }

    @Override
    public EvaluationExpression group(EvaluationExpression expression, BooleanOperation operation) {
        if (isGroupable(expression, operation)) {
            if (expression instanceof BooleanEvaluationExpression) {
                return expression.group(this, operation);
            } else if (expression instanceof TagSetEvaluationExpression) {
                Set<Tag> ids = new HashSet<>(((TagSetEvaluationExpression)expression).getTags());
                switch (operation) {
                    case AND:
                        ids.retainAll(tags);
                        return ids.isEmpty() ? new BooleanEvaluationExpression(false) : new TagSetEvaluationExpression(ids);
                    case OR:
                        ids.addAll(tags);
                        return new TagSetEvaluationExpression(ids);
                }
            }
        }
        throw new UnsupportedOperationException("Expressions can not be grouped together");
    }

    public static EvaluationExpression create(Set<String> tags) {
        if (tags == null) {
            return new BooleanEvaluationExpression(true);
        } else if (tags.isEmpty()) {
            return new BooleanEvaluationExpression(false);
        } else {
            return new TagSetEvaluationExpression(tags.stream().map(Tag::new).collect(Collectors.toSet()));
        }
    }
}
