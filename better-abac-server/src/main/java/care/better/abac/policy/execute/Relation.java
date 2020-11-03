package care.better.abac.policy.execute;

import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;

/**
 * @author Andrej Dolenc
 */
public class Relation {
    @Getter
    private final String name;
    @Getter
    private final boolean inverse;

    /**
     * Deduces relation direction from name prefix - '<' for inverse (target -> source)
     * , '>' or nothing for default source -> target
     *
     * @param name - relation name with optional prefix
     */
    public Relation(@NonNull String name) {
        this(StringUtils.stripStart(name, "<>"), name.startsWith("<"));
    }

    public Relation(@NonNull String name, boolean inverse) {
        Preconditions.checkArgument(!name.isBlank(), "Relation name is blank!");
        this.name = name;
        this.inverse = inverse;
    }
}
