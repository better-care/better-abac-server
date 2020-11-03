package care.better.abac.jpa.repo;

import care.better.abac.jpa.QueryDslRepository;
import care.better.abac.jpa.entity.RelationType;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

/**
 * @author Bostjan Lah
 */
//@RepositoryRestResource(path = "/relationType")
public interface RelationTypeRepository extends CrudRepository<RelationType, Long>, QueryDslRepository<RelationType, Long> {
    RelationType findByName(@Param("name") String name);
}
