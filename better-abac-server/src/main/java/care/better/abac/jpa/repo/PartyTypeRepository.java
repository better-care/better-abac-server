package care.better.abac.jpa.repo;

import care.better.abac.jpa.QueryDslRepository;
import care.better.abac.jpa.entity.Party;
import care.better.abac.jpa.entity.PartyType;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

/**
 * @author Bostjan Lah
 */
//@RepositoryRestResource(path = "/partyType")
public interface PartyTypeRepository extends CrudRepository<PartyType, Long>, QueryDslRepository<PartyType, Long> {
    PartyType findByName(@Param("name") String name);
}
