package care.better.abac.plugin;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.pf4j.PluginManager;
import org.pf4j.ServiceProviderExtensionFinder;

/**
 * @author Andrej Dolenc
 */
public class PluginServiceProviderExtensionFinder extends ServiceProviderExtensionFinder {

    public PluginServiceProviderExtensionFinder(PluginManager pluginManager) {
        super(pluginManager);
    }

    @Override
    public Map<String, Set<String>> readClasspathStorages() {
        return new HashMap<>();
    }
}
