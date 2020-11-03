package care.better.abac.dto.config;

import com.google.common.collect.ImmutableSet;

import java.util.Set;

/**
 * @author Matic Ribic
 */
public abstract class AbstractExternalSystemDto {
    private String name;

    private String abacRestBaseUrl;

    private Set<ExternalPolicyDto> policies = ImmutableSet.of();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAbacRestBaseUrl() {
        return abacRestBaseUrl;
    }

    public void setAbacRestBaseUrl(String abacRestBaseUrl) {
        this.abacRestBaseUrl = abacRestBaseUrl;
    }

    public Set<ExternalPolicyDto> getPolicies() {
        return policies;
    }

    public void setPolicies(Set<ExternalPolicyDto> policies) {
        this.policies = ImmutableSet.copyOf(policies);
    }
}
