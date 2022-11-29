package care.better.abac.jpa.repo;

import care.better.abac.jpa.QueryDslRepository;
import care.better.abac.jpa.QueryListFilter;
import care.better.abac.jpa.entity.QRelationType;
import care.better.abac.jpa.entity.RelationType;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.querydsl.core.types.dsl.BooleanExpression;
import org.springframework.data.repository.query.Param;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * @author Bostjan Lah
 */
public interface RelationTypeRepository extends EntityCrudRepository<RelationType>, QueryDslRepository<RelationType, Long> {
    RelationType findByName(@Param("name") String name);

    @SuppressWarnings("MethodWithMultipleLoops")
    default List<RelationType> findAllByPartyTypeIds(QueryListFilter<String> reportTypeNamesFilter, Collection<Long> partyTypeIdsParam) {
        if (partyTypeIdsParam.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> partyTypeIds = PersistenceUtil.wrapInConditionToList(partyTypeIdsParam);
        List<RelationType> relationTypeEntities = new ArrayList<>();

        for (List<Long> allowedSourceIdsPartition : Lists.partition(partyTypeIds, PersistenceUtil.PARTITION_SIZE)) {
            for (List<Long> allowedTargetIdsPartition : Lists.partition(partyTypeIds, PersistenceUtil.PARTITION_SIZE)) {
                Iterables.addAll(relationTypeEntities, findAll(reportTypeNamesFilter, allowedSourceIdsPartition, allowedTargetIdsPartition));
            }
        }

        return relationTypeEntities;
    }

    default List<RelationType> findAll(QueryListFilter<String> reportTypeNamesFilter, List<Long> allowedSourceIds, List<Long> allowedTargetIds) {
        if (allowedSourceIds.isEmpty() || allowedTargetIds.isEmpty()) {
            return Collections.emptyList();
        }

        BooleanExpression allowedTypesPredicate = QRelationType.relationType.allowedSource.id.in(allowedSourceIds)
                .or(QRelationType.relationType.allowedTarget.id.in(allowedTargetIds));

        if (reportTypeNamesFilter.isEnabled()) {
            List<String> reportTypeNames = reportTypeNamesFilter.getFilter();
            if (reportTypeNames.isEmpty()) {
                return Collections.emptyList();
            }

            List<RelationType> relationTypeEntities = new ArrayList<>();
            for (List<String> reportTypeNamesPartition : Lists.partition(reportTypeNames, PersistenceUtil.PARTITION_SIZE)) {
                Iterables.addAll(relationTypeEntities, findAll(allowedTypesPredicate.and(QRelationType.relationType.name.in(reportTypeNamesPartition))));
            }

            return relationTypeEntities;
        } else {
            return ImmutableList.copyOf(findAll(allowedTypesPredicate));
        }
    }

    @Override
    default Optional<RelationType> update(Long id, RelationType submittedRelationType) {
        return findById(id).map(entity -> {
            if (submittedRelationType.getName() != null) {
                entity.setName(submittedRelationType.getName());
            }
            if (submittedRelationType.getAllowedSource() != null) {
                entity.setAllowedSource(submittedRelationType.getAllowedSource());
            }
            if (submittedRelationType.getAllowedTarget() != null) {
                entity.setAllowedTarget(submittedRelationType.getAllowedTarget());
            }

            return save(entity);
        });
    }
}
