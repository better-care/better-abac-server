package care.better.abac.dto;

import care.better.abac.plugin.PluginManager.Key;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.EqualsAndHashCode.Include;

import java.time.OffsetDateTime;

/**
 * @author Andrej Dolenc
 */
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
@Data
public class PluginStateDto extends DtoWithId {
    public PluginStateDto(Long id, String pluginId, String serviceId, boolean initialized, OffsetDateTime syncTime) {
        super(id);
        this.pluginId = pluginId;
        this.serviceId = serviceId;
        this.initialized = initialized;
        this.syncTime = syncTime;
    }

    public PluginStateDto(Long id, Key key, boolean initialized, OffsetDateTime syncTime) {
        super(id);
        pluginId = key.getPluginId();
        serviceId = key.getServiceId();
        this.initialized = initialized;
        this.syncTime = syncTime;
    }

    @Include
    private String pluginId;
    @Include
    private String serviceId;
    private boolean initialized;
    private OffsetDateTime syncTime;

    public Key getKey() {
        return Key.of(pluginId, serviceId);
    }
}
