package care.better.abac.dto.content;

import com.google.common.collect.ImmutableList;

import java.util.Collections;
import java.util.List;

/**
 * @author Matic Ribic
 */
public class AppContentSyncResultDto {
    private List<DtoSyncResult<PlainPartyTypeDto>> partyTypes = Collections.emptyList();
    private List<DtoSyncResult<PlainPartyDto>> parties = Collections.emptyList();
    private List<DtoSyncResult<PlainRelationTypeDto>> relationTypes = Collections.emptyList();
    private List<DtoSyncResult<PlainPartyRelationDto>> partyRelations = Collections.emptyList();
    private List<DtoSyncResult<PlainPolicyDto>> policies = Collections.emptyList();

    public List<DtoSyncResult<PlainPartyTypeDto>> getPartyTypes() {
        return partyTypes;
    }

    public void setPartyTypes(List<DtoSyncResult<PlainPartyTypeDto>> partyTypes) {
        this.partyTypes = ImmutableList.copyOf(partyTypes);
    }

    public List<DtoSyncResult<PlainPartyDto>> getParties() {
        return parties;
    }

    public void setParties(List<DtoSyncResult<PlainPartyDto>> parties) {
        this.parties = ImmutableList.copyOf(parties);
    }

    public List<DtoSyncResult<PlainRelationTypeDto>> getRelationTypes() {
        return relationTypes;
    }

    public void setRelationTypes(List<DtoSyncResult<PlainRelationTypeDto>> relationTypes) {
        this.relationTypes = ImmutableList.copyOf(relationTypes);
    }

    public List<DtoSyncResult<PlainPartyRelationDto>> getPartyRelations() {
        return partyRelations;
    }

    public void setPartyRelations(List<DtoSyncResult<PlainPartyRelationDto>> partyRelations) {
        this.partyRelations = ImmutableList.copyOf(partyRelations);
    }

    public List<DtoSyncResult<PlainPolicyDto>> getPolicies() {
        return policies;
    }

    public void setPolicies(List<DtoSyncResult<PlainPolicyDto>> policies) {
        this.policies = ImmutableList.copyOf(policies);
    }

    public boolean isEmpty() {
        return partyTypes.isEmpty() && parties.isEmpty() && relationTypes.isEmpty() && partyRelations.isEmpty() && policies.isEmpty();
    }
}
