package care.better.abac.jpa.repo;

import care.better.abac.dto.config.ExternalSystemValidationStatus;
import care.better.abac.jpa.QueryDslRepository;
import care.better.abac.jpa.entity.ExternalSystemEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * @author Matic Ribic
 */
public interface ExternalSystemRepository extends CrudRepository<ExternalSystemEntity, Long>, QueryDslRepository<ExternalSystemEntity, Long> {

    @Query("SELECT DISTINCT es FROM ExternalSystemEntity es LEFT JOIN FETCH es.policies WHERE es.externalId = :externalId")
    ExternalSystemEntity findByExternalId(@Param("externalId") String externalId);

    @Query("SELECT DISTINCT es FROM ExternalSystemEntity es LEFT JOIN FETCH es.policies WHERE es.validationStatus = :status")
    List<ExternalSystemEntity> findByValidationStatus(@Param("status") ExternalSystemValidationStatus validationStatus);
}
