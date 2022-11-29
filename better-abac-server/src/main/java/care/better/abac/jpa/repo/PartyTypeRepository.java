package care.better.abac.jpa.repo;

import care.better.abac.jpa.QueryDslRepository;
import care.better.abac.jpa.QueryListFilter;
import care.better.abac.jpa.entity.PartyType;
import care.better.abac.jpa.entity.QPartyType;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.springframework.data.repository.query.Param;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author Bostjan Lah
 */
public interface PartyTypeRepository extends EntityCrudRepository<PartyType>, QueryDslRepository<PartyType, Long> {
    PartyType findByName(@Param("name") String name);

    @Override
    default Optional<PartyType> update(Long id, PartyType submittedPartyType) {
        return findById(id).map(entity -> {
            entity.setName(submittedPartyType.getName());
            return entity;
        });
    }

    default List<PartyType> findAllByNames(QueryListFilter<String> nameFilter) {
        if (nameFilter.isEnabled()) {
            List<PartyType> partyTypes = new ArrayList<>();
            for (List<String> namesPartition : Lists.partition(nameFilter.getFilter(), PersistenceUtil.PARTITION_SIZE)) {
                Iterables.addAll(partyTypes, findAll(QPartyType.partyType.name.in(namesPartition)));
            }

            return partyTypes;
        } else {
            return ImmutableList.copyOf(findAll());
        }
    }
}
