package care.better.abac.plugin.listener;

import care.better.abac.dto.PluginStateDto;
import care.better.abac.plugin.EndpointType;
import care.better.abac.plugin.PluginManager;
import care.better.abac.plugin.PluginManager.Key;
import care.better.abac.plugin.SynchronizationPhase;
import care.better.abac.plugin.SynchronizationTaskRunner;
import care.better.abac.plugin.condition.ConditionalOnServiceType;
import care.better.abac.plugin.config.PluginConfiguration;
import care.better.abac.plugin.shedlock.RunnableWithLockConfiguration;
import care.better.abac.plugin.shedlock.ShedlockConfiguration;
import care.better.abac.plugin.spi.AsyncPartyRelationService;
import com.google.common.base.Preconditions;
import lombok.NonNull;
import net.javacrumbs.shedlock.core.LockConfiguration;
import net.javacrumbs.shedlock.spring.LockableTaskScheduler;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.util.UriUtils;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Duration;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static care.better.abac.ValidationUtils.isValidURL;
import static care.better.abac.plugin.listener.PartyRelationAsyncServiceRestController.STATIC_PATH;

/**
 * @author Andrej Dolenc
 */
@SuppressWarnings("rawtypes")
@Configuration
@ConditionalOnServiceType(AsyncPartyRelationService.class)
@Import({PartyRelationAsyncServiceRestController.class, ShedlockConfiguration.class})
@AutoConfigureAfter(PluginConfiguration.class)
public class ListenerServiceAutoConfiguration {
    private static final long RETRY_INTERVAL_MS = 10000L;

    @Autowired
    public void lazyAsyncServiceConfigurator(
            @Value("${async.rest.callback.port:}") String callbackPort,
            @Value("${spring.application.name:}") String applicationName,
            @Value("${server.port:}") String serverPort,
            @Value("${async.rest.base-callback-url:#{null}}") String baseCallbackUrl,
            @NonNull PluginManager pluginManager,
            @NonNull SynchronizationTaskRunner synchronizationTaskRunner,
            @NonNull LockableTaskScheduler lockableTaskScheduler,
            @NonNull PartyRelationAsyncServiceRestController controller) throws UnknownHostException {

        Map<Key, AsyncPartyRelationService<?>> pluginServices = pluginManager.getServicesOfType(AsyncPartyRelationService.class)
                .entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> (AsyncPartyRelationService<?>)entry.getValue()));
        Preconditions.checkArgument(pluginServices.values().stream().allMatch(service -> service.getEndpointType() == EndpointType.REST),
                                    String.format("Only services with endpoint type %s are supported!", EndpointType.REST.name()));
        String baseUrl;
        if (StringUtils.isNotBlank(baseCallbackUrl)) {
            baseUrl = getBaseUrl(baseCallbackUrl);
        } else {
            String implicitBaseUrl = getBaseUrl(serverPort, null);
            if (StringUtils.isBlank(implicitBaseUrl)) {
                Preconditions.checkArgument(StringUtils.isNotBlank(callbackPort), "async.rest.callback.port property required!");
                Preconditions.checkArgument(StringUtils.isNotBlank(applicationName), "spring.application.name property required!");
            }
            String explicitBaseUrl = getBaseUrl(callbackPort, applicationName);
            if (StringUtils.isNotBlank(implicitBaseUrl) && StringUtils.isNotBlank(explicitBaseUrl) && !StringUtils.equals(implicitBaseUrl, explicitBaseUrl)) {
                throw new IllegalArgumentException(String.format(
                        "Ambiguous callback URLs defined:\n %s \n %s \n" +
                                "Check the following properties: server.port %s " +
                                "spring.application.name %s " +
                                "and async.rest.callback.port %s", implicitBaseUrl, explicitBaseUrl, serverPort, applicationName, callbackPort));
            } else {
                baseUrl = StringUtils.isNotBlank(implicitBaseUrl) ? implicitBaseUrl : explicitBaseUrl;
            }
        }
        Preconditions.checkArgument(isValidURL(baseUrl), String.format("Invalid listener URL: %s , check async.rest.base-callback-url property!", baseUrl));
        lockableTaskScheduler.scheduleWithFixedDelay(new AsyncPartyRelationServiceConfigurator(controller, pluginServices, synchronizationTaskRunner, baseUrl), RETRY_INTERVAL_MS);
    }

    private String getBaseUrl(String port, String applicationName) throws UnknownHostException {
        if (StringUtils.isBlank(port) || StringUtils.equals("0", port)) {
            return "";
        }
        String infixUrl = StringUtils.isNotBlank(applicationName) ? ('/' + applicationName) : "";
        return getBaseUrl("http://" + InetAddress.getLocalHost().getHostAddress() + ':' + port + infixUrl);
    }

    private String getBaseUrl(String externalApplicationUrl) {
        return StringUtils.removeEnd(externalApplicationUrl, "/") + STATIC_PATH;
    }

    private static final class AsyncPartyRelationServiceConfigurator implements RunnableWithLockConfiguration {
        private final PartyRelationAsyncServiceRestController controller;
        private final Map<Key, AsyncPartyRelationService<?>> pluginServices;
        private final SynchronizationTaskRunner synchronizationTaskRunner;
        private final Set<Key> configuredServices = new HashSet<>();
        private final Set<Key> initializedServices = new HashSet<>();
        private final String baseUrl;

        private AsyncPartyRelationServiceConfigurator(
                @NonNull PartyRelationAsyncServiceRestController controller,
                @NonNull Map<Key, AsyncPartyRelationService<?>> pluginServices,
                @NonNull SynchronizationTaskRunner synchronizationTaskRunner,
                @NonNull String baseUrl) {
            this.controller = controller;
            this.pluginServices = pluginServices;
            this.synchronizationTaskRunner = synchronizationTaskRunner;
            this.baseUrl = StringUtils.appendIfMissing(baseUrl, "/");
        }

        @Override
        public LockConfiguration getLockConfiguration() {
            return new LockConfiguration("AsyncPartyRelationRestServiceConfigurator",
                                         Duration.ofMillis(RETRY_INTERVAL_MS * SynchronizationPhase.INITIAL.getLockCycleDuration()),
                                         Duration.ofMillis(RETRY_INTERVAL_MS));
        }

        @Override
        public void run() {
            initializeServices();
            Map<String, AsyncPartyRelationService<?>> services = configureListenerEndpoint();
            if (!services.isEmpty()) {
                controller.addListenerServices(services);
            }
        }

        private void initializeServices() {
            pluginServices.entrySet()
                    .stream()
                    .filter(e -> !initializedServices.contains(e.getKey()))
                    .map(entry -> synchronizationTaskRunner.syncInitial(entry.getValue(), entry.getKey()))
                    .filter(PluginStateDto::isInitialized)
                    .map(PluginStateDto::getKey)
                    .forEach(initializedServices::add);
        }

        private Map<String, AsyncPartyRelationService<?>> configureListenerEndpoint() {
            return pluginServices.entrySet().stream()
                    .filter(entry -> initializedServices.contains(entry.getKey()))
                    .filter(entry -> !configuredServices.contains(entry.getKey()))
                    .map(entry -> Pair.of(UriUtils.encodePath(entry.getValue().getId(), "UTF-8"), entry))
                    .peek(p -> p.getValue().getValue().configureListenerEndpoint(baseUrl + p.getKey()))
                    .peek(p -> configuredServices.add(p.getValue().getKey()))
                    .collect(Collectors.toMap(Pair::getKey, p -> p.getValue().getValue()));
        }
    }
}
