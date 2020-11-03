package care.better.abac.jpa.repo;

import care.better.abac.jpa.QueryDslRepository;
import care.better.abac.jpa.entity.Party;
import care.better.abac.jpa.entity.PartyRelation;
import care.better.abac.jpa.entity.QParty;
import care.better.abac.jpa.entity.QPartyRelation;
import care.better.abac.policy.execute.Relation;
import com.querydsl.jpa.impl.JPAQuery;
import lombok.NonNull;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;

/**
 * @author Bostjan Lah
 */
//@RepositoryRestResource(path = "/partyRelation")
public interface PartyRelationRepository extends CrudRepository<PartyRelation, Long>, QueryDslRepository<PartyRelation, Long> {

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
            //query.where(QPartyRelation.partyRelation.source.externalIds.any().in(sourceIds));
        }
        if (targetIds != null) {
            query.innerJoin(QPartyRelation.partyRelation.target.externalIds).on(QPartyRelation.partyRelation.target.externalIds.any().in(targetIds));
            //query.where(QPartyRelation.partyRelation.target.externalIds.any().in(targetIds));
        }
        if (relations != null) {
            query.innerJoin(QPartyRelation.partyRelation.relationType).on(QPartyRelation.partyRelation.relationType.name.in(relations));
            //query.where(QPartyRelation.partyRelation.relationType.name.in(relations));
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
        for (int i = 1; i < relations.size(); i++)
        {
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
    int deleteByPartyAndRelationType(@Param("sourceExternalId") String sourceExternalId, @Param("targetExternalId") String targetExternalId, @Param("relationName") String relationName);
}
