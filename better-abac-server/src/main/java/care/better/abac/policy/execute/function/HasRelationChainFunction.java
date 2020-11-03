package care.better.abac.policy.execute.function;

import com.google.common.collect.Sets;
import care.better.abac.jpa.entity.QPartyRelation;
import care.better.abac.jpa.repo.PartyRelationRepository;
import care.better.abac.policy.execute.Executable;
import care.better.abac.policy.execute.Executable.Type;
import care.better.abac.policy.execute.ExecutableFunction;
import care.better.abac.policy.execute.Relation;
import care.better.abac.policy.execute.evaluation.BooleanEvaluationExpression;
import care.better.abac.policy.execute.evaluation.EvaluationExpression;
import care.better.abac.policy.execute.evaluation.ResultSetEvaluationExpression;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.jpa.impl.JPAQuery;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static care.better.abac.policy.execute.function.Functions.convertIds;
import static care.better.abac.policy.execute.function.Functions.convertRelationChain;

/**
 * @author Andrej Dolenc
 */
@Component
public class HasRelationChainFunction extends ExecutableFunction {
    @Resource
    private PartyRelationRepository partyRelationRepository;

    @Transactional(readOnly = true)
    @Executable(type = Type.EVALUATE)
    public EvaluationExpression hasRelationChainEvaluate(String sourceId, Object targetIds, Object... relationNames) {
        Collection<String> ids = convertIds(targetIds);
        return new BooleanEvaluationExpression(
        !ids.isEmpty() && findBySourceAndTargetExternalIdsAndRelationChain(Collections.singleton(sourceId), ids, OffsetDateTime.now(),
                                                                           convertRelationChain(relationNames)));
    }

    @Transactional(readOnly = true)
    @Executable(type = Type.QUERY)
    public EvaluationExpression hasRelationChainQuery(String sourceId, Object targetIds, Object... relationNames) {
        return ResultSetEvaluationExpression.create(queryBySourceAndTargetExternalIdsAndRelationChain(Collections.singleton(sourceId), convertIds(targetIds), OffsetDateTime.now(),
                                                          convertRelationChain(relationNames)));
    }

    private boolean findBySourceAndTargetExternalIdsAndRelationChain(
            Collection<String> sourceIds,
            Collection<String> targetIds,
            OffsetDateTime validUntil,
            List<Relation> relations) {
        return partyRelationRepository.createRelationChainQuery(sourceIds, targetIds, validUntil, relations)
                .select(QPartyRelation.partyRelation.id)
                .fetchFirst() != null;
    }

    private Set<String> queryBySourceAndTargetExternalIdsAndRelationChain(
            Collection<String> sourceIds,
            Collection<String> targetIds,
            OffsetDateTime validUntil,
            List<Relation> relations) {
        if (Functions.isQueryParam(sourceIds)) {
            JPAQuery<?> query = partyRelationRepository.createRelationChainQuery(null, targetIds, validUntil, relations);
            return Sets.newHashSet(query.select(extractQueryPath(relations)).distinct().fetch());
        } else if (Functions.isQueryParam(targetIds)) {
            List<Relation> reverse = Functions.reverse(relations);
            JPAQuery<?> query = partyRelationRepository.createRelationChainQuery(null, sourceIds, validUntil, reverse);
            return Sets.newHashSet(query.select(extractQueryPath(reverse)).distinct().fetch());
        } else {
            return findBySourceAndTargetExternalIdsAndRelationChain(sourceIds, targetIds, validUntil, relations) ? null : Collections.emptySet();
        }
    }

    private StringPath extractQueryPath(List<Relation> relations) {
        return relations.get(0).isInverse() ? QPartyRelation.partyRelation.target.externalIds.any() : QPartyRelation.partyRelation.source.externalIds.any();
    }
}
