package care.better.abac.plugin.sync;

import care.better.abac.plugin.PluginManager;
import care.better.abac.plugin.PluginManager.Key;
import care.better.abac.plugin.SynchronizationPhase;
import care.better.abac.plugin.SynchronizationTaskRunner;
import care.better.abac.plugin.config.PluginConfiguration;
import care.better.abac.plugin.shedlock.RunnableWithLockConfiguration;
import care.better.abac.plugin.shedlock.ShedlockConfiguration;
import care.better.abac.plugin.spi.SynchronizingPartyRelationService;
import lombok.NonNull;
import net.javacrumbs.shedlock.core.LockConfiguration;
import net.javacrumbs.shedlock.spring.LockableTaskScheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

/**
 * @author Andrej Dolenc
 */
@Configuration
@Import(ShedlockConfiguration.class)
@AutoConfigureAfter(PluginConfiguration.class)
public class SynchronizingServiceAutoConfiguration {
    private static final long SYNC_INTERVAL_MS = 10000L;

    @Autowired
    public void configurePartyRelationSyncTasks(
            @NonNull PluginManager manager,
            @NonNull SynchronizationTaskRunner taskRunner,
            @NonNull LockableTaskScheduler lockableTaskScheduler) {
        Map<Key, SynchronizingPartyRelationService> pluginServices = manager.getServicesOfType(SynchronizingPartyRelationService.class);

        pluginServices.entrySet().stream()
                .map(entry ->
                             new RunnableWithLockConfiguration() {
                                 @Override
                                 public void run() {
                                     Instant to = Instant.now();
                                     taskRunner.syncInitial(entry.getValue(), entry.getKey(), to);
                                 }

                                 @Override
                                 public LockConfiguration getLockConfiguration() {
                                     return createLockConfiguration(entry.getKey(), SynchronizationPhase.INITIAL);
                                 }
                             })
                .forEach(r -> lockableTaskScheduler.scheduleWithFixedDelay(r, SYNC_INTERVAL_MS));
        pluginServices.entrySet().stream()
                .map(entry ->
                             new RunnableWithLockConfiguration() {

                                 @Override
                                 public void run() {
                                     Instant to = Instant.now();
                                     taskRunner.sync(entry.getValue(), entry.getKey(), to);
                                 }

                                 @Override
                                 public LockConfiguration getLockConfiguration() {
                                     return createLockConfiguration(entry.getKey(), SynchronizationPhase.PERIODIC);
                                 }
                             })
                .forEach(r -> lockableTaskScheduler.scheduleWithFixedDelay(r, SYNC_INTERVAL_MS));
    }

    private LockConfiguration createLockConfiguration(Key key, SynchronizationPhase phase) {
        return ShedlockConfiguration.create(key, phase, Duration.ofMillis(SYNC_INTERVAL_MS * phase.getLockCycleDuration()), Duration.ofMillis(SYNC_INTERVAL_MS));
    }
}
