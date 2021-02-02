package care.better.abac.plugin;

import care.better.abac.dto.PluginStateDto;
import care.better.abac.plugin.PluginManager.Key;
import care.better.abac.plugin.spi.AsyncPartyRelationService;
import care.better.abac.plugin.spi.SynchronizingPartyRelationService;
import lombok.NonNull;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;

/**
 * @author Andrej Dolenc
 */
public class SynchronizationTaskRunner {
    private static final Logger log = LogManager.getLogger(SynchronizationTaskRunner.class);

    private final PluginStateManager stateManager;
    private final PartyRelationSynchronizer synchronizer;
    private final PartyRelationServiceInitializer initializer;

    public SynchronizationTaskRunner(
            @NonNull PluginStateManager stateManager,
            @NonNull PartyRelationSynchronizer synchronizer,
            @NonNull PartyRelationServiceInitializer initializer) {
        this.stateManager = stateManager;
        this.synchronizer = synchronizer;
        this.initializer = initializer;
    }

    @Transactional
    public void sync(SynchronizingPartyRelationService service, @NonNull Key key, Instant to) {
        PluginStateDto state = stateManager.getPluginState(key);
        if (state.isInitialized()) {
            Instant from = state.getSyncTime().toInstant();
            log.debug("Running sync task for {} between {} and {} .", service.providesFor(), from, to);
            synchronizer.sync(service, from, to);
            state.setSyncTime(OffsetDateTime.ofInstant(to, ZoneId.systemDefault()));
            stateManager.savePluginState(state);
            log.debug("Sync task completed!");
        } else {
            log.debug("Initial state synchronization for {} not yet completed!", service.providesFor());
        }
    }

    @Transactional
    public PluginStateDto syncInitial(@NonNull SynchronizingPartyRelationService service, @NonNull Key key, @NonNull Instant startTime) {
        PluginStateDto pluginState = stateManager.getPluginState(key);
        if (!pluginState.isInitialized()) {
            initializer.initialize(service);
            synchronizer.syncInitial(service, startTime);
            pluginState.setSyncTime(OffsetDateTime.ofInstant(startTime, ZoneId.systemDefault()));
            pluginState.setInitialized(true);
            stateManager.savePluginState(pluginState);
        }
        return pluginState;
    }

    @Transactional
    public PluginStateDto syncInitial(@NonNull AsyncPartyRelationService<?> service, @NonNull Key key) {
        PluginStateDto pluginState = stateManager.getPluginState(key);
        if (!pluginState.isInitialized()) {
            initializer.initialize(service);
            synchronizer.syncInitial(service);
            pluginState.setSyncTime(OffsetDateTime.ofInstant(Instant.now(), ZoneId.systemDefault()));
            pluginState.setInitialized(true);
            stateManager.savePluginState(pluginState);
        }
        return pluginState;
    }
}
