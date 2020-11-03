package care.better.abac.policy.execute.function;

import com.google.common.collect.Sets;
import care.better.abac.jpa.entity.QPartyRelation;
import care.better.abac.jpa.repo.PartyRelationRepository;
import care.better.abac.policy.execute.Executable;
import care.better.abac.policy.execute.Executable.Type;
import care.better.abac.policy.execute.ExecutableFunction;
import care.better.abac.policy.execute.evaluation.BooleanEvaluationExpression;
import care.better.abac.policy.execute.evaluation.EvaluationExpression;
import care.better.abac.policy.execute.evaluation.ResultSetEvaluationExpression;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import static care.better.abac.policy.execute.function.Functions.convertIds;
import static care.better.abac.policy.execute.function.Functions.convertRelationNames;

/**
 * @author Andrej Dolenc
 */
@Component
public class HasRelationFunction extends ExecutableFunction {
    @Resource
    private PartyRelationRepository partyRelationRepository;

    @Transactional(readOnly = true)
    @Executable(type = Type.EVALUATE)
    public EvaluationExpression hasRelationEvaluate(String sourceId, String relationName, Object targetIds) {
        Collection<String> ids = convertIds(targetIds);
        return new BooleanEvaluationExpression(!ids.isEmpty() && findBySourceAndTargetExternalIdsAndRelations(Collections.singleton(sourceId), ids, OffsetDateTime.now(),
                                                                       convertRelationNames(relationName)));
    }

    @Transactional(readOnly = true)
    @Executable(type = Type.QUERY)
    public EvaluationExpression hasRelationQuery(String sourceId, String relationName, Object targetIds) {
        return ResultSetEvaluationExpression.create(queryBySourceOrTargetExternalIdsAndRelations(Collections.singleton(sourceId), convertIds(targetIds), OffsetDateTime.now(),
                                                     convertRelationNames(relationName)));
    }

    private boolean findBySourceAndTargetExternalIdsAndRelations(
            Collection<String> sourceIds,
            Collection<String> targetIds,
            OffsetDateTime validUntil,
            Collection<String> relations) {
        return partyRelationRepository.createRelationQuery(sourceIds, targetIds, validUntil, relations)
                .select(QPartyRelation.partyRelation.id)
                .fetchFirst() != null;
    }

    private Set<String> queryBySourceOrTargetExternalIdsAndRelations(
            Collection<String> sourceIds,
            Collection<String> targetIds,
            OffsetDateTime validUntil,
            Collection<String> relations) {
        if (Functions.isQueryParam(sourceIds)) {
            return Sets.newHashSet(partyRelationRepository.createRelationQuery(null, targetIds, validUntil, relations)
                                           .select(QPartyRelation.partyRelation.source.externalIds.any()).distinct().fetch());
        } else if (Functions.isQueryParam(targetIds)) {
            return Sets.newHashSet(partyRelationRepository.createRelationQuery(sourceIds, null, validUntil, relations)
                                           .select(QPartyRelation.partyRelation.target.externalIds.any()).distinct().fetch());
        } else {
            return findBySourceAndTargetExternalIdsAndRelations(sourceIds, targetIds, validUntil, relations) ? null : Collections.emptySet();
        }
    }
}
