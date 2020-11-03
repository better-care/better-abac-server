package care.better.abac.plugin.spi;

import java.util.Set;

import care.better.abac.plugin.PartyRelationChange;
import care.better.abac.plugin.RelationType;

/**
 * @author Andrej Dolenc
 */
public interface OnDemandPartyRelationService extends PartyRelationService {
    Set<PartyRelationChange> get(RelationType relationType, String sourceExternalId, String targetExternalId);
    Set<PartyRelationChange> get(String sourceExternalId, String targetExternalId);
}
