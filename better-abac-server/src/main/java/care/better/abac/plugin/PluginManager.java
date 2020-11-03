package care.better.abac.plugin;

import care.better.abac.plugin.auth.AuthorizationProvider;
import care.better.abac.plugin.spi.Service;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import org.apache.commons.lang3.tuple.Pair;
import org.pf4j.CompoundPluginDescriptorFinder;
import org.pf4j.DefaultExtensionFinder;
import org.pf4j.DefaultPluginManager;
import org.pf4j.ExtensionFinder;
import org.pf4j.ManifestPluginDescriptorFinder;
import org.pf4j.PluginDescriptorFinder;
import org.pf4j.PluginState;
import org.pf4j.PropertiesPluginDescriptorFinder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * @author Andrej Dolenc
 */
public class PluginManager extends DefaultPluginManager {
    private final Map<Key, Descriptor> pluginServices;

    public PluginManager() {
        pluginServices = initializePlugins();
    }

    @Override
    protected PluginDescriptorFinder createPluginDescriptorFinder() {
        return new CompoundPluginDescriptorFinder()
                .add(new PropertiesPluginDescriptorFinder())
                .add(new FilenamePluginDescriptionFinder())
                .add(new ManifestPluginDescriptorFinder());
    }

    public <T extends Service> Map<Key, T> getServicesOfType(Class<T> serviceClass) {
        return pluginServices.entrySet()
                .stream()
                .filter(entry -> serviceClass.isAssignableFrom(entry.getValue().getService().getClass()))
                .collect(Collectors.toMap(Entry::getKey, entry -> serviceClass.cast(entry.getValue().getService())));
    }

    public void configureServices(@NonNull Optional<AuthorizationProvider> authorizationProvider) {
        pluginServices.values().forEach(
                descriptor -> descriptor.getService().configure(descriptor.getConfiguration(), authorizationProvider.orElse(null)));
    }

    @Override
    protected ExtensionFinder createExtensionFinder() {
        DefaultExtensionFinder finder = (DefaultExtensionFinder)super.createExtensionFinder();
        finder.add(new PluginServiceProviderExtensionFinder(this));
        return finder;
    }

    private Map<Key, Descriptor> initializePlugins() {
        loadPlugins();
        startPlugins();
        return createServices();
    }

    private Map<Key, Descriptor> createServices() {
        return getPlugins().stream()
                .filter(p -> PluginState.STARTED == p.getPluginState())
                .map(p ->
                     {
                         Properties properties = new Properties();
                         Path propertyPath = Paths.get(getPluginsRoot().toAbsolutePath().toString(),
                                                       p.getDescriptor().getPluginId() + ".properties");
                         if (propertyPath.toFile().exists()) {
                             try {
                                 properties.load(Files.newBufferedReader(propertyPath));
                             } catch (IOException ignored) {
                             }
                         }
                         List<Service> extensions = getExtensions(Service.class, p.getPluginId());
                         return extensions.stream()
                                 .map(e -> Pair.of(Key.of(p.getPluginId(), e.getId()), Descriptor.of(e, properties)))
                                 .collect(Collectors.toList());
                     }).flatMap(Collection::stream).collect(Collectors.toMap(Pair::getKey, Pair::getValue));
    }

    @AllArgsConstructor(staticName = "of")
    @EqualsAndHashCode
    @Getter
    public static final class Key {
        private final String pluginId;
        private final String serviceId;

        public String getId() {
            return pluginId + "::" + serviceId;
        }
    }

    @AllArgsConstructor(staticName = "of")
    @Getter
    public static final class Descriptor {
        private final Service service;
        private final Properties configuration;
    }
}
