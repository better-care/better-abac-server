package care.better.abac.jpa.repo;

import care.better.abac.jpa.QueryDslRepository;
import care.better.abac.jpa.entity.Party;
import care.better.abac.jpa.entity.PartyRelation;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

/**
 * @author Bostjan Lah
 */
//@RepositoryRestResource(path = "/party")
public interface PartyRepository extends CrudRepository<Party, Long>, QueryDslRepository<Party, Long> {
    List<Party> findByExternalIds(String externalId);

    @Query("SELECT p FROM Party p JOIN p.type t JOIN p.externalIds i WHERE t.name = :type and i = :externalId")
    Party findByTypeAndExternalId(@Param("type") String type, @Param("externalId") String externalId);

    @Query("SELECT p FROM Party p JOIN p.type t JOIN p.externalIds i WHERE t.name = :type and i IN (:externalIds)")
    Party findByTypeAndExternalId(@Param("type") String type, @Param("externalIds") Collection<String> externalIds);
}
