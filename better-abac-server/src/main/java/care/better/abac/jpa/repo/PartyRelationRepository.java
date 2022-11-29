package care.better.abac.jpa.repo;

import care.better.abac.jpa.QueryDslRepository;
import care.better.abac.jpa.entity.Party;
import care.better.abac.jpa.entity.PartyRelation;
import care.better.abac.jpa.entity.QParty;
import care.better.abac.jpa.entity.QPartyRelation;
import care.better.abac.policy.execute.Relation;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.querydsl.jpa.impl.JPAQuery;
import lombok.NonNull;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * @author Bostjan Lah
 */
public interface PartyRelationRepository extends EntityCrudRepository<PartyRelation>, QueryDslRepository<PartyRelation, Long> {

    default JPAQuery<?> createBaseQuery(@NonNull OffsetDateTime validUntil) {
        return createBaseQuery(QPartyRelation.partyRelation, validUntil);
    }

    default JPAQuery<?> createBaseQuery(@NonNull QPartyRelation qPartyRelation, @NonNull OffsetDateTime validUntil) {
        return getQueryFactory()
                .from(qPartyRelation)
                .where(qPartyRelation.validUntil.isNull().or(qPartyRelation.validUntil.goe(validUntil)));
    }

    default JPAQuery<?> createRelationQuery(
            Collection<String> sourceIds,
            Collection<String> targetIds,
            @NonNull OffsetDateTime validUntil,
            Collection<String> relations) {
        JPAQuery<?> query = createBaseQuery(validUntil);
        if (sourceIds != null) {
            query.innerJoin(QPartyRelation.partyRelation.source.externalIds).on(QPartyRelation.partyRelation.source.externalIds.any().in(sourceIds));
        }
        if (targetIds != null) {
            query.innerJoin(QPartyRelation.partyRelation.target.externalIds).on(QPartyRelation.partyRelation.target.externalIds.any().in(targetIds));
        }
        if (relations != null) {
            query.innerJoin(QPartyRelation.partyRelation.relationType).on(QPartyRelation.partyRelation.relationType.name.in(relations));
        }
        return query;
    }

    default JPAQuery<?> createRelationChainQuery(
            Collection<String> sourceIds,
            Collection<String> targetIds,
            @NonNull OffsetDateTime validUntil,
            @NonNull List<Relation> relations) {
        QParty endChain;
        Relation relation = relations.get(0);
        QPartyRelation qPartyRelationChain = QPartyRelation.partyRelation;
        JPAQuery<?> query = createBaseQuery(qPartyRelationChain, validUntil);
        query.where(qPartyRelationChain.relationType.name.eq(relation.getName()));
        JPAQuery<?> endQuery = query;
        if (relation.isInverse()) {
            if (sourceIds != null) {
                query.where(qPartyRelationChain.target.externalIds.any().in(sourceIds));
            }
            endChain = qPartyRelationChain.source;
        } else {
            if (sourceIds != null) {
                query.where(qPartyRelationChain.source.externalIds.any().in(sourceIds));
            }
            endChain = qPartyRelationChain.target;
        }
        for (int i = 1; i < relations.size(); i++) {
            qPartyRelationChain = new QPartyRelation("partyRelation_" + i);
            relation = relations.get(i);
            JPAQuery<Party> chainQuery = createBaseQuery(qPartyRelationChain, validUntil).select(relation.isInverse()
                                                                                                         ? qPartyRelationChain.target
                                                                                                         : qPartyRelationChain.source);
            chainQuery.where(qPartyRelationChain.relationType.name.eq(relation.getName()));
            endQuery.where(endChain.in(chainQuery));
            if (relation.isInverse()) {
                endChain = qPartyRelationChain.source;
            } else {
                endChain = qPartyRelationChain.target;
            }
            endQuery = chainQuery;
        }
        if (targetIds != null) {
            if (relation.isInverse()) {
                endQuery.where(qPartyRelationChain.source.externalIds.any().in(targetIds));
            } else {
                endQuery.where(qPartyRelationChain.target.externalIds.any().in(targetIds));
            }
        }
        return query;
    }

    @Query("SELECT tid FROM PartyRelation pr " +
            "JOIN pr.source s " +
            "JOIN s.externalIds sid " +
            "JOIN pr.target t " +
            "JOIN pr.relationType rt " +
            "JOIN t.externalIds tid " +
            "WHERE (pr.validUntil IS NULL OR pr.validUntil >= :validUntil) AND sid = :sourceId AND rt.name  = :relationName")
    List<String> findTargetIds(@Param("sourceId") String sourceId, @Param("relationName") String relationName, @Param("validUntil") OffsetDateTime validUntil);

    @Modifying
    @Query("DELETE FROM PartyRelation pr " +
            " WHERE pr.id IN " +
            "       (SELECT pr.id " +
            "          FROM PartyRelation pr " +
            "          JOIN pr.source   s " +
            "          JOIN s.externalIds sid " +
            "          JOIN pr.target   t " +
            "          JOIN t.externalIds tid " +
            "          JOIN pr.relationType  rt " +
            "         WHERE rt.name = :relationName " +
            "           AND (sid = :sourceExternalId OR :sourceExternalId IS NULL) " +
            "           AND (tid = :targetExternalId OR :targetExternalId IS NULL))"

    )
    int deleteByPartyAndRelationType(
            @Param("sourceExternalId") String sourceExternalId,
            @Param("targetExternalId") String targetExternalId,
            @Param("relationName") String relationName);

    @SuppressWarnings("MethodWithMultipleLoops")
    default List<PartyRelation> findAllByPartyAndRelationTypeIds(Collection<Long> partyIds, Collection<Long> relationTypeIds) {
        List<PartyRelation> partyRelations = new ArrayList<>();

        for (List<Long> relationTypesIdsPartition : Lists.partition(PersistenceUtil.wrapInConditionToList(relationTypeIds), PersistenceUtil.PARTITION_SIZE)) {
            for (List<Long> sourcePartyIdsPartition : Lists.partition(PersistenceUtil.wrapInConditionToList(partyIds), PersistenceUtil.PARTITION_SIZE)) {
                for (List<Long> targetPartyIdsPartition : Lists.partition(PersistenceUtil.wrapInConditionToList(partyIds), PersistenceUtil.PARTITION_SIZE)) {
                    Iterables.addAll(partyRelations,
                                     findAllByPartyAndRelationTypeIds(sourcePartyIdsPartition, targetPartyIdsPartition, relationTypesIdsPartition));
                }
            }
        }

        return partyRelations;
    }

    @Query("SELECT DISTINCT pr FROM PartyRelation pr LEFT JOIN FETCH pr.relationType rt " +
            "LEFT JOIN FETCH pr.source s LEFT JOIN FETCH s.externalIds " +
            "LEFT JOIN FETCH pr.target t LEFT JOIN FETCH t.externalIds " +
            "WHERE rt.id in (:relationTypeIds) AND s.id in (:sourceIds) AND t.id in (:targetIds)")
    List<PartyRelation> findAllByPartyAndRelationTypeIds(Collection<Long> sourceIds, Collection<Long> targetIds, Collection<Long> relationTypeIds);

    @Override
    default Optional<PartyRelation> update(Long id, PartyRelation submittedPartyRelation) {
        PartyRelationValidator.validate(submittedPartyRelation);
        return findById(id).map(entity -> {
            entity.setSource(submittedPartyRelation.getSource());
            entity.setTarget(submittedPartyRelation.getTarget());
            entity.setRelationType(submittedPartyRelation.getRelationType());
            entity.setValidUntil(submittedPartyRelation.getValidUntil());

            return entity;
        });
    }
}
