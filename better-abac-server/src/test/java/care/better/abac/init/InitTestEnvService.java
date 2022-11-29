package care.better.abac.init;

import care.better.abac.jpa.entity.Party;
import care.better.abac.jpa.entity.PartyRelation;
import care.better.abac.jpa.entity.PartyType;
import care.better.abac.jpa.entity.Policy;
import care.better.abac.jpa.entity.RelationType;
import care.better.abac.jpa.repo.PartyRelationRepository;
import care.better.abac.jpa.repo.PartyRepository;
import care.better.abac.jpa.repo.PartyTypeRepository;
import care.better.abac.jpa.repo.RelationTypeRepository;
import care.better.abac.policy.service.PolicyService;
import care.better.abac.rest.MappingUtils;

import javax.inject.Inject;
import java.util.Set;

/**
 * @author Matic Ribič
 */
public class InitTestEnvService {

    @Inject
    private PartyTypeRepository partyTypeRepository;

    @Inject
    private PartyRepository partyRepository;

    @Inject
    private RelationTypeRepository relationTypeRepository;

    @Inject
    private PartyRelationRepository partyRelationRepository;

    @Inject
    private PolicyService policyService;

    public PartyType createPartyType(PartyType partyType) {
        return partyTypeRepository.save(partyType);
    }

    public PartyType createPartyType(String name) {
        PartyType partyType = new PartyType();
        partyType.setName(name);
        return createPartyType(partyType);
    }

    private PartyType findPartyTypeByName(String type) {
        PartyType partyType = partyTypeRepository.findByName(type);
        if (partyType == null) {
            throw new IllegalArgumentException("Missing party type: " + type);
        }
        return partyType;
    }

    public PartyRelation createPartyRelation(PartyRelation partyRelation) {
        return partyRelationRepository.save(partyRelation);
    }

    public Party createParty(Party party) {
        return partyRepository.save(party);
    }

    public Party createParty(String type, Set<String> externalIds) {
        Party party = new Party();
        party.setType(findPartyTypeByName(type));
        MappingUtils.synchronizeSets(externalIds, party.getExternalIds());
        return partyRepository.save(party);
    }

    public Policy createPolicy(Policy policy) {
        return policyService.create(policy);
    }

    public Policy createPolicy(String name, String policy) {
        Policy policyEntity = new Policy();
        policyEntity.setName(name);
        policyEntity.setPolicy(policy);
        return createPolicy(policyEntity);
    }

    public RelationType createRelationType(RelationType relationType) {
        return relationTypeRepository.save(relationType);
    }

    public RelationType createRelationType(String name, String allowedSourcePartyType, String allowedTargetPartyType) {
        RelationType relationType = new RelationType();
        relationType.setName(name);
        relationType.setAllowedSource(findPartyTypeByName(allowedSourcePartyType));
        relationType.setAllowedTarget(findPartyTypeByName(allowedTargetPartyType));
        return createRelationType(relationType);
    }

    public void deleteAll() {
        partyRelationRepository.deleteAll();
        relationTypeRepository.deleteAll();
        partyRepository.deleteAll();
        partyTypeRepository.deleteAll();
        policyService.findAll().forEach(policy -> policyService.deleteById(policy.getId()));
    }
}
