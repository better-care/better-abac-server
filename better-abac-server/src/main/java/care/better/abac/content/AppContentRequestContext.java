package care.better.abac.content;

import care.better.abac.dto.content.AppContentResultLogLevel;
import care.better.abac.jpa.entity.EntityWithId;
import com.google.common.collect.ImmutableList;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Matic Ribic
 */
public class AppContentRequestContext {
    private final List<String> includedPartyTypes;
    private final List<String> includedRelationTypes;
    private final boolean dryRun;
    private final AppContentResultLogLevel resultLogLevel;
    private final Map<String, Set<Class<? extends EntityWithId>>> visitedEntityTypesByActions = new HashMap<>();

    public AppContentRequestContext(
            Collection<String> includedPartyTypes,
            Collection<String> includedRelationTypes,
            boolean dryRun,
            AppContentResultLogLevel resultLogLevel) {
        this.includedPartyTypes = ImmutableList.copyOf(includedPartyTypes);
        this.includedRelationTypes = ImmutableList.copyOf(includedRelationTypes);
        this.dryRun = dryRun;
        this.resultLogLevel = resultLogLevel;
    }

    public List<String> getIncludedPartyTypes() {
        return includedPartyTypes;
    }

    public boolean isPartyTypeIncluded(String type) {
        return includedPartyTypes.isEmpty() || includedPartyTypes.contains(type);
    }

    public List<String> getIncludedRelationTypes() {
        return includedRelationTypes;
    }

    public boolean isRelationTypeIncluded(String type) {
        return includedRelationTypes.isEmpty() || includedRelationTypes.contains(type);
    }

    public boolean isDryRun() {
        return dryRun;
    }

    public AppContentResultLogLevel getResultLogLevel() {
        return resultLogLevel;
    }

    public <T extends EntityWithId> boolean isVisited(Class<T> type, String action) {
        return visitedEntityTypesByActions.getOrDefault(action, Collections.emptySet()).contains(type);
    }

    public <T extends EntityWithId> void markAsVisited(Class<T> type, String action) {
        visitedEntityTypesByActions.computeIfAbsent(action, ignored -> new HashSet<>()).add(type);
    }

    @Override
    public String toString() {
        return String.format("AppContentRequestContext{includedPartyTypes=%s, includedRelationTypes=%s, dryRun=%s, resultLogLevel=%s}",
                             includedPartyTypes,
                             includedRelationTypes,
                             dryRun,
                             resultLogLevel);
    }
}
