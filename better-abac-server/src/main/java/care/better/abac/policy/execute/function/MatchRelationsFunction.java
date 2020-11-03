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
import com.querydsl.jpa.impl.JPAQuery;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Set;

/**
 * @author Andrej Dolenc
 */
@Component
public class MatchRelationsFunction extends ExecutableFunction {
    @Resource
    private PartyRelationRepository partyRelationRepository;

    @Transactional(readOnly = true)
    @Executable(type = Type.EVALUATE)
    public EvaluationExpression matchRelationsEvaluate(String sourceId, String sourceRelationName, String targetId, String targetRelationName) {
        Pair<JPAQuery<?>, JPAQuery<?>> query = createMatchRelationsQuery(sourceId, sourceRelationName, targetId, targetRelationName, OffsetDateTime.now());
        return new BooleanEvaluationExpression(query.getLeft().select(QPartyRelation.partyRelation.id.eqAny(query.getRight().select(QPartyRelation.partyRelation.id))).fetchFirst());
    }

    @Transactional(readOnly = true)
    @Executable(type = Type.QUERY)
    public EvaluationExpression matchRelationsQuery(String sourceId, String sourceRelationName, String targetId, String targetRelationName) {
        OffsetDateTime validUntil = OffsetDateTime.now();
        if (Functions.isQueryParam(sourceId)) {
            Pair<JPAQuery<?>, JPAQuery<?>> queries = createMatchRelationsQuery(null, sourceRelationName, targetId, targetRelationName, validUntil);
            return ResultSetEvaluationExpression.create(intersect(queries.getLeft(), queries.getRight(), true));
        } else if (Functions.isQueryParam(targetId)) {
            Pair<JPAQuery<?>, JPAQuery<?>> queries = createMatchRelationsQuery(sourceId, sourceRelationName, null, targetRelationName, validUntil);
            return ResultSetEvaluationExpression.create(intersect(queries.getLeft(), queries.getRight(), false));
        } else {
            return matchRelationsEvaluate(sourceId, sourceRelationName, targetId, targetRelationName);
        }
    }

    private Set<String> intersect(JPAQuery<?> sourceQuery, JPAQuery<?> targetQuery, boolean collectSource) {
        Set<Long> sourceIds = Sets.newHashSet(sourceQuery.select(QPartyRelation.partyRelation.id).distinct().fetch());
        Set<Long> targetIds = Sets.newHashSet(targetQuery.select(QPartyRelation.partyRelation.id).distinct().fetch());
        sourceIds.retainAll(targetIds);
        JPAQuery<?> query = partyRelationRepository.getQueryFactory().query().from(QPartyRelation.partyRelation).where(QPartyRelation.partyRelation.id.in(sourceIds));
        if (collectSource) {
            return Sets.newHashSet(query.select(QPartyRelation.partyRelation.source.externalIds.any()).distinct().fetch());
        } else {
            return Sets.newHashSet(query.select(QPartyRelation.partyRelation.target.externalIds.any()).distinct().fetch());
        }
    }

    private Pair<JPAQuery<?>, JPAQuery<?>> createMatchRelationsQuery(
            String sourceId,
            String sourceRelationName,
            String targetId,
            String targetRelationName,
            OffsetDateTime validUntil) {
        JPAQuery<?> sourceQuery = partyRelationRepository.createRelationQuery(sourceId != null ? Collections.singleton(sourceId) : null, null, validUntil,
                                                                              Collections.singleton(sourceRelationName));
        JPAQuery<?> targetQuery = partyRelationRepository.createRelationQuery(null, targetId != null ? Collections.singleton(targetId) : null, validUntil,
                                                                              Collections.singleton(targetRelationName));
        return Pair.of(sourceQuery, targetQuery);
    }
}
