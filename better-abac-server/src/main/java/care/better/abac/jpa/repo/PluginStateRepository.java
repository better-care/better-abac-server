package care.better.abac.jpa.repo;

import care.better.abac.jpa.QueryDslRepository;
import care.better.abac.jpa.entity.PluginState;
import org.springframework.data.repository.CrudRepository;

/**
 * @author Andrej Dolenc
 */
public interface PluginStateRepository extends CrudRepository<PluginState, Long>, QueryDslRepository<PluginState, Long> {
    PluginState findByPluginIdAndServiceId(String pluginId, String serviceId);
}
