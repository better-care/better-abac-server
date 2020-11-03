package care.better.abac.policy.execute.evaluation;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;

/**
 * @author Andrej Dolenc
 */
@EqualsAndHashCode
public class Tag {
    public static final String DELIMETER = "::";

    @Getter
    private final String tag;
    @Getter
    private final String value;

    @JsonCreator
    public Tag(@JsonProperty("tag") String tag, @JsonProperty("value") String value) {
        this.tag = tag;
        this.value = value;
    }

    public Tag(@NonNull String tag) {
        String[] split = tag.split(DELIMETER, 2);
        this.tag = split[0];
        value = split.length == 2 ? split[1] : null;
    }
}
