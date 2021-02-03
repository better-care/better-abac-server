package care.better.abac.plugin.config;

import care.better.abac.jpa.repo.PartyRelationRepository;
import care.better.abac.jpa.repo.PartyRepository;
import care.better.abac.jpa.repo.PartyTypeRepository;
import care.better.abac.jpa.repo.PluginStateRepository;
import care.better.abac.jpa.repo.RelationTypeRepository;
import care.better.abac.plugin.PartyChangeMapper;
import care.better.abac.plugin.PartyRelationServiceInitializer;
import care.better.abac.plugin.PartyRelationSynchronizer;
import care.better.abac.plugin.PluginManager;
import care.better.abac.plugin.PluginStateManager;
import care.better.abac.plugin.SynchronizationTaskRunner;
import care.better.abac.plugin.auth.AuthorizationProvider;
import care.better.abac.plugin.listener.ListenerServiceAutoConfiguration;
import care.better.abac.plugin.sync.SynchronizingServiceAutoConfiguration;
import lombok.NonNull;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Optional;

/**
 * @author Andrej Dolenc
 */
@Configuration
@ImportAutoConfiguration({SynchronizingServiceAutoConfiguration.class, ListenerServiceAutoConfiguration.class})
public class PluginConfiguration {

    @Bean
    public PluginStateManager pluginStateManager(
            @NonNull PluginStateRepository pluginStateRepository,
            @NonNull Optional<AuthorizationProvider> authorizationProvider,
            @NonNull PluginManager pluginManager) {
        pluginManager.configureServices(authorizationProvider);
        return new PluginStateManager(pluginStateRepository);
    }

    @Bean
    public PluginManager pluginManager() {
        return new PluginManager();
    }

    @Bean
    public PartyRelationServiceInitializer partyRelationServiceInitializer(
            @NonNull PartyTypeRepository partyTypeRepository,
            @NonNull RelationTypeRepository relationTypeRepository) {
        return new PartyRelationServiceInitializer(partyTypeRepository, relationTypeRepository);
    }

    @Bean
    public PartyChangeMapper partyChangeMapper(@NonNull PartyRepository partyRepository, @NonNull PartyTypeRepository partyTypeRepository) {
        return new PartyChangeMapper(partyRepository, partyTypeRepository);
    }

    @Bean
    public PartyRelationSynchronizer partyRelationSynchronizer(
            @NonNull PartyRelationServiceInitializer partyRelationServiceInitializer,
            @NonNull PartyRelationRepository partyRelationRepository,
            @NonNull PartyRepository partyRepository,
            @NonNull RelationTypeRepository relationTypeRepository,
            @NonNull PartyChangeMapper partyChangeMapper) {
        return new PartyRelationSynchronizer(partyRelationServiceInitializer,
                                             partyRelationRepository,
                                             partyRepository,
                                             relationTypeRepository,
                                             partyChangeMapper);
    }

    @Bean
    public SynchronizationTaskRunner synchronizationTaskRunner(
            @NonNull PluginStateManager stateManager,
            @NonNull PartyRelationSynchronizer synchronizer,
            @NonNull PartyRelationServiceInitializer initializer) {
        return new SynchronizationTaskRunner(stateManager, synchronizer, initializer);
    }
}
