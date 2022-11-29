package care.better.abac.jpa.repo;

import care.better.abac.exception.PartyRelationInvalidTypesException;
import care.better.abac.jpa.entity.PartyRelation;
import care.better.abac.jpa.entity.PartyType;

/**
 * @author Matic Ribic
 */
public final class PartyRelationValidator {

    private PartyRelationValidator() {
    }

    public static void validate(PartyRelation partyRelation) {
        PartyType allowedSource = partyRelation.getRelationType().getAllowedSource();
        if (allowedSource != null && !allowedSource.getName().equals(partyRelation.getSource().getType().getName())) {
            throw new PartyRelationInvalidTypesException(
                    "Invalid source party type: " + partyRelation.getSource().getType().getName() + ", allowed type: " + allowedSource.getName() + '!');
        }
        PartyType allowedTarget = partyRelation.getRelationType().getAllowedTarget();
        if (allowedTarget != null && !allowedTarget.getName().equals(partyRelation.getTarget().getType().getName())) {
            throw new PartyRelationInvalidTypesException(
                    "Invalid target party type: " + partyRelation.getTarget().getType().getName() + ", allowed type: " + allowedTarget.getName() + '!');
        }
    }

}
