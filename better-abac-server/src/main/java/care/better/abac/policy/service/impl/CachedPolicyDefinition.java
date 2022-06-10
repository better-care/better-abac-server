package care.better.abac.policy.service.impl;

import care.better.abac.policy.definition.PolicyDefinition;

public class CachedPolicyDefinition {
    private final long loadedAtSystemNano = System.nanoTime();
    private final int entityVersion;
    private final PolicyDefinition policyDefinition;

    public CachedPolicyDefinition(int entityVersion, PolicyDefinition policyDefinition) {
        this.entityVersion = entityVersion;
        this.policyDefinition = policyDefinition;
    }

    public PolicyDefinition getPolicyDefinition() {
        return policyDefinition;
    }

    public boolean isNotOlderThan(long maxAgeInMillis) {
        return (System.nanoTime() - loadedAtSystemNano) / 1_000_000L <= maxAgeInMillis;
    }

    public boolean isOfVersion(Integer version) {
        return version != null && entityVersion == version;
    }
}
