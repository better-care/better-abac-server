package care.better.abac.external;

import care.better.abac.external.keycloak.KeycloakPartyInfoService;
import care.better.abac.jpa.entity.ExternalSystemEntity;
import care.better.abac.rest.client.ValidationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * @author Matic Ribic
 */
public class ExternalSystemConfigurationScheduler {
    private static final Logger log = LogManager.getLogger(KeycloakPartyInfoService.class.getName());
    private final ExternalSystemService externalSystemService;

    public ExternalSystemConfigurationScheduler(ExternalSystemService externalSystemService) {
        this.externalSystemService = externalSystemService;
    }

    @Scheduled(fixedDelayString = "${abac.configValidationPeriodInMillis:60000}")
    public void validate() {
        externalSystemService.getConfigListToValidate().stream().map(ExternalSystemEntity::getExternalId).forEach(externalId -> {
            try {
                externalSystemService.validate(externalId, true);
            } catch (ValidationException e) {
                log.error("Validation of configuration {} failed: {}", externalId, e.getMessage());
            }
        });
    }
}
