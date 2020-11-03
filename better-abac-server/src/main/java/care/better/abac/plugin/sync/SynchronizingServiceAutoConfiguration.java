package care.better.abac.plugin.sync;

import com.marand.core.Opt;
import care.better.abac.dto.PluginStateDto;
import care.better.abac.jpa.repo.PartyRelationRepository;
import care.better.abac.jpa.repo.PartyRepository;
import care.better.abac.jpa.repo.RelationTypeRepository;
import care.better.abac.plugin.PartyChangeMapper;
import care.better.abac.plugin.PartyRelationServiceInitializer;
import care.better.abac.plugin.PluginManager;
import care.better.abac.plugin.PluginManager.Key;
import care.better.abac.plugin.PluginStateManager;
import care.better.abac.plugin.condition.ConditionalOnServiceType;
import care.better.abac.plugin.config.PluginConfiguration;
import care.better.abac.plugin.spi.SynchronizingPartyRelationService;
import care.better.abac.plugin.sync.shedlock.Shedlock;
import lombok.NonNull;
import net.javacrumbs.shedlock.core.DefaultLockManager;
import net.javacrumbs.shedlock.core.LockConfiguration;
import net.javacrumbs.shedlock.core.LockManager;
import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.jdbctemplate.JdbcTemplateLockProvider;
import net.javacrumbs.shedlock.spring.LockableTaskScheduler;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.sql.DataSource;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.stream.Collectors;

/**
 * @author Andrej Dolenc
 */
@Configuration
@EnableScheduling
@ConditionalOnServiceType(SynchronizingPartyRelationService.class)
@EntityScan(basePackageClasses = Shedlock.class)
public class SynchronizingServiceAutoConfiguration {
    private static final long SYNC_INTERVAL_MS = 10000;

    @Bean
    public PartyRelationSynchronizer partyRelationSynchronizer(
            @NonNull PartyRelationRepository partyRelationRepository,
            @NonNull PartyRepository partyRepository,
            @NonNull RelationTypeRepository relationTypeRepository,
            @NonNull PartyChangeMapper partyChangeMapper) {
        return new PartyRelationSynchronizer(partyRelationRepository, partyRepository, relationTypeRepository, partyChangeMapper);
    }

    @Bean
    public SynchronizationTaskRunner synchronizationTaskRunner(
            @NonNull PluginStateManager stateManager,
            @NonNull PartyRelationSynchronizer synchronizer,
            @NonNull PartyRelationServiceInitializer initializer) {
        return new SynchronizationTaskRunner(stateManager, synchronizer, initializer);
    }

    @Bean
    public LockProvider lockProvider(@NonNull DataSource dataSource) {
        return new JdbcTemplateLockProvider(
                JdbcTemplateLockProvider.Configuration.builder()
                        .withJdbcTemplate(new JdbcTemplate(dataSource))
                        .usingDbTime()
                        .build()
        );
    }

    private LockableTaskScheduler createLockableTaskScheduler(TaskScheduler taskScheduler, LockProvider lockProvider) {
        LockManager manager = new DefaultLockManager(lockProvider,
                                                     r -> Opt.of(r)
                                                             .toType(RunnableWithLockConfiguration.class)
                                                             .map(RunnableWithLockConfiguration::getLockConfiguration)
                                                             .toOptional());
        return new LockableTaskScheduler(taskScheduler, manager);
    }

    @Bean
    public List<ScheduledFuture<?>> configurePartyRelationSyncTasks(
            @NonNull PluginManager manager,
            @NonNull SynchronizationTaskRunner taskRunner,
            @NonNull LockProvider lockProvider,
            @NonNull TaskScheduler taskScheduler) {
        LockableTaskScheduler lockableTaskScheduler = createLockableTaskScheduler(taskScheduler, lockProvider);
        Map<Key, SynchronizingPartyRelationService> pluginServices = manager.getServicesOfType(SynchronizingPartyRelationService.class);
        List<ScheduledFuture<?>> initTasks = pluginServices.entrySet().stream()
                .map(entry ->
                             new RunnableWithLockConfiguration() {
                                 @Override
                                 public void run() {
                                     Instant to = Instant.now();
                                     taskRunner.syncInitial(entry.getValue(), entry.getKey(), to);
                                 }

                                 @Override
                                 public LockConfiguration getLockConfiguration() {
                                     return createLockConfiguration(entry.getKey());
                                 }
                             })
                .map(r -> lockableTaskScheduler.scheduleAtFixedRate(r, SYNC_INTERVAL_MS * 10))
                .collect(Collectors.toList());

        List<ScheduledFuture<?>> syncTasks = pluginServices.entrySet().stream()
                .map(entry ->
                             new RunnableWithLockConfiguration() {

                                 @Override
                                 public void run() {
                                     Instant to = Instant.now();
                                     taskRunner.sync(entry.getValue(), entry.getKey(), to);
                                 }

                                 @Override
                                 public LockConfiguration getLockConfiguration() {
                                     return createLockConfiguration(entry.getKey());
                                 }
                             })
                .map(r -> lockableTaskScheduler.scheduleWithFixedDelay(r, SYNC_INTERVAL_MS))
                .collect(Collectors.toList());
        initTasks.addAll(syncTasks);
        return initTasks;
    }

    private LockConfiguration createLockConfiguration(Key key)
    {
        String id = key.getId().length() <= 255 ? key.getId() : Integer.toString(key.hashCode());
        return new LockConfiguration(id, Duration.ofMillis(SYNC_INTERVAL_MS * 10), Duration.ofMillis(SYNC_INTERVAL_MS));
    }
}
