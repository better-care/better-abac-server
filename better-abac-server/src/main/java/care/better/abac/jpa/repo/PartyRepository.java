package care.better.abac.jpa.repo;

import care.better.abac.jpa.QueryDslRepository;
import care.better.abac.jpa.entity.Party;
import care.better.abac.rest.MappingUtils;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * @author Bostjan Lah
 */
public interface PartyRepository extends EntityCrudRepository<Party>, QueryDslRepository<Party, Long> {
    List<Party> findByExternalIds(String externalId);

    @Query("SELECT p FROM Party p JOIN p.type t JOIN p.externalIds i WHERE t.name = :type and i = :externalId")
    Party findByTypeAndExternalId(@Param("type") String type, @Param("externalId") String externalId);

    @Query("SELECT p FROM Party p JOIN p.type t JOIN p.externalIds i WHERE t.name = :type and i IN (:externalIds)")
    Party findByTypeAndExternalId(@Param("type") String type, @Param("externalIds") Collection<String> externalIds);

    @Query("SELECT i FROM Party p JOIN p.type t JOIN p.externalIds i WHERE t.name = :type")
    Set<String> findExternalIdsByType(@Param("type") String type);

    @Query("SELECT DISTINCT p FROM Party p LEFT JOIN FETCH p.type t LEFT JOIN FETCH p.externalIds WHERE t.id in (:partyTypeIds)")
    List<Party> executePartitionFindAllByTypeIds(Collection<Long> partyTypeIds);

    default List<Party> findAllByTypeIds(Collection<Long> partyTypeIds) {
        List<Party> parties = new ArrayList<>();

        for (List<Long> partyTypesIdsPartition : Lists.partition(PersistenceUtil.wrapInConditionToList(partyTypeIds), PersistenceUtil.PARTITION_SIZE)) {
            Iterables.addAll(parties, executePartitionFindAllByTypeIds(partyTypesIdsPartition));
        }

        return parties;

    }

    @Override
    default Optional<Party> update(Long id, Party submittedParty) {
        return findById(id).map(entity -> {
            entity.setType(submittedParty.getType());
            if (submittedParty.getExternalIds() != null) {
                MappingUtils.synchronizeSets(submittedParty.getExternalIds(), entity.getExternalIds());
            }

            return entity;
        });
    }
}
