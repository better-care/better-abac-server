package care.better.abac.plugin.spi;

import care.better.abac.plugin.auth.AuthorizationProvider;

import java.util.Properties;

/**
 * @author Andrej Dolenc
 */
public interface Service {
    default String getId() {
        return getClass().getName();
    }

    void configure(Properties properties, AuthorizationProvider authorizationProvider);
}
