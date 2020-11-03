package care.better.abac.plugin;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

/**
 * @author Andrej Dolenc
 */
@EqualsAndHashCode
@ToString
public class PartyRelationChange {

    private final Set<String> sourceExternalIds;

    private final Set<String> targetExternalIds;
    @Getter
    private final RelationType relationType;
    @Getter
    private final ChangeType changeType;
    @Getter
    private final OffsetDateTime validUntil;

    public PartyRelationChange(
            Set<String> sourceExternalIds,
            Set<String> targetExternalIds,
            @NonNull RelationType relationType,
            @NonNull ChangeType changeType,
            OffsetDateTime validUntil) {
        this.sourceExternalIds = sourceExternalIds == null ? null : new HashSet<>(sourceExternalIds);
        this.targetExternalIds = targetExternalIds == null ? null : new HashSet<>(targetExternalIds);
        this.relationType = relationType;
        this.changeType = changeType;
        this.validUntil = validUntil;
    }

    public Set<String> getSourceExternalId() {
        return sourceExternalIds == null ? null : Collections.unmodifiableSet(sourceExternalIds);
    }

    public Set<String> getTargetExternalId() {
        return targetExternalIds == null ? null : Collections.unmodifiableSet(targetExternalIds);
    }
}
