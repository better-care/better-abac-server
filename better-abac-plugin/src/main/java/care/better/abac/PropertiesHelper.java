package care.better.abac;

import lombok.NonNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @author Andrej Dolenc
 */
public final class PropertiesHelper {
    private PropertiesHelper() {
    }

    /**
     * Returns new Properties subset including only keys that start with certain prefix.
     *
     * @param properties properties
     * @param prefix     property prefix
     * @param keepPrefix keeps prefix if true, otherwise strips it from the key
     * @return new Properties subset
     */
    public static Properties getPropertiesByPrefix(@NonNull Properties properties, String prefix, boolean keepPrefix) {

        if (prefix == null || prefix.isEmpty()) {
            return new Properties(properties);
        }

        String prefixMatch = prefix.endsWith(".") ? prefix : prefix + '.';
        return properties.stringPropertyNames().stream().filter(key -> key.startsWith(prefixMatch))
                .reduce(new Properties(), (p, key) -> {
                    p.setProperty(keepPrefix ? key : key.substring(prefixMatch.length()), properties.getProperty(key));
                    return p;
                }, (p1, p2) -> {
                    p1.putAll(p2);
                    return p1;
                });
    }

    /**
     * Maps property group with certain prefix to Property map, using the next value after prefix in property path as key.
     * Example:
     * <br>
     * <pre>
     *  prefix.a.name = namea
     *  prefix.a.value = valuea
     *
     *  prefix.b.name = nameb
     *  prifix.b.value = valueb
     * </pre>
     * Is mapped to:
     * <pre>
     *  A:
     *  name = namea
     *  value = valuea
     *  B:
     *  name = nameb
     *  value = valueb
     * </pre>
     *
     * @param properties property prefix
     * @param prefix     property prefix
     * @return property map
     */
    public static Map<String, Properties> getPropertyMap(@NonNull Properties properties, String prefix) {
        Properties prefixedProperties = getPropertiesByPrefix(properties, prefix, false);
        Map<String, Properties> propertiesMap = new HashMap<>();
        prefixedProperties.stringPropertyNames()
                .stream()
                .filter(key -> key.split("\\.").length > 1)
                .map(key -> key.split("\\.")[0].trim())
                .forEach(key -> propertiesMap.computeIfAbsent(key, groupKey -> getPropertiesByPrefix(prefixedProperties, groupKey, false)));
        return propertiesMap;
    }
}
