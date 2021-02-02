package care.better.abac.external;

import care.better.abac.jpa.entity.ExternalSystemEntity;
import care.better.abac.plugin.SynchronizationPhase;
import care.better.abac.plugin.shedlock.RunnableWithLockConfiguration;
import care.better.abac.plugin.shedlock.ShedlockConfiguration;
import care.better.abac.rest.client.ValidationException;
import net.javacrumbs.shedlock.core.LockConfiguration;
import net.javacrumbs.shedlock.spring.LockableTaskScheduler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

/**
 * @author Matic Ribic
 */
@Configuration
@Import(ShedlockConfiguration.class)
@ConditionalOnProperty(name = "abac.configValidationEnabled", havingValue = "true")
public class ExternalSystemSchedulerConfiguration {

    @Bean
    public ValidationTaskRunner validationTaskRunner(ExternalSystemService externalSystemService) {
        return new ValidationTaskRunner(externalSystemService);
    }

    @Autowired
    public void createValidationTask(
            @Value("${abac.configValidationPeriodInMillis:60000}") long periodMillis,
            ValidationTaskRunner validationTaskRunner,
            LockableTaskScheduler lockableTaskScheduler) {
        lockableTaskScheduler.scheduleWithFixedDelay(new RunnableWithLockConfiguration() {
            @Override
            public void run() {
                validationTaskRunner.validate();
            }

            @Override
            public LockConfiguration getLockConfiguration() {
                return new LockConfiguration("ExternalSystemConfigurationValidator",
                                             Duration.ofMillis(periodMillis * SynchronizationPhase.PERIODIC.getLockCycleDuration()),
                                             Duration.ofMillis(periodMillis));
            }
        }, periodMillis);
    }

    public static class ValidationTaskRunner {
        private final Logger log = LogManager.getLogger(ValidationTaskRunner.class);

        private final ExternalSystemService externalSystemService;

        public ValidationTaskRunner(ExternalSystemService externalSystemService) {
            this.externalSystemService = externalSystemService;
        }

        @Transactional
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
}
