package care.better.abac.dto.content;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.ImmutableList;

import java.util.Collections;
import java.util.List;

/**
 * @author Matic Ribic
 */
public class AppContentDto {
    private List<PlainPartyTypeDto> partyTypes = Collections.emptyList();
    private List<PlainPartyDto> parties = Collections.emptyList();
    private List<PlainRelationTypeDto> relationTypes = Collections.emptyList();
    private List<PlainPartyRelationDto> partyRelations = Collections.emptyList();
    private List<PlainPolicyDto> policies = Collections.emptyList();

    public List<PlainPartyTypeDto> getPartyTypes() {
        return partyTypes;
    }

    public void setPartyTypes(List<PlainPartyTypeDto> partyTypes) {
        this.partyTypes = ImmutableList.copyOf(partyTypes);
    }

    public List<PlainPartyDto> getParties() {
        return parties;
    }

    public void setParties(List<PlainPartyDto> parties) {
        this.parties = ImmutableList.copyOf(parties);
    }

    public List<PlainRelationTypeDto> getRelationTypes() {
        return relationTypes;
    }

    public void setRelationTypes(List<PlainRelationTypeDto> relationTypes) {
        this.relationTypes = ImmutableList.copyOf(relationTypes);
    }

    public List<PlainPartyRelationDto> getPartyRelations() {
        return partyRelations;
    }

    public void setPartyRelations(List<PlainPartyRelationDto> partyRelations) {
        this.partyRelations = ImmutableList.copyOf(partyRelations);
    }

    public List<PlainPolicyDto> getPolicies() {
        return policies;
    }

    public void setPolicies(List<PlainPolicyDto> policies) {
        this.policies = ImmutableList.copyOf(policies);
    }

    @JsonIgnore
    public boolean isEmpty() {
        return partyTypes.isEmpty() && parties.isEmpty() && relationTypes.isEmpty() && partyRelations.isEmpty() && policies.isEmpty();
    }
}
