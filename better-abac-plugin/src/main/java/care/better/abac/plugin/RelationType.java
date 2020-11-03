package care.better.abac.plugin;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

/**
 * @author Andrej Dolenc
 */
@EqualsAndHashCode
@ToString
public class RelationType {

    @Getter
    private final String name;
    @Getter
    private final String sourcePartyType;
    @Getter
    private final String targetPartyType;

    public RelationType(
            @NonNull String name,
            @NonNull String sourcePartyType,
            @NonNull String targetPartyType) {
        this.name = name;
        this.sourcePartyType = sourcePartyType;
        this.targetPartyType = targetPartyType;
    }
}
