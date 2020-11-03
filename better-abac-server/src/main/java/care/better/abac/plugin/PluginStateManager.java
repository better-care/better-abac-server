package care.better.abac.plugin;

import care.better.abac.dto.PluginStateDto;
import care.better.abac.jpa.entity.PluginState;
import care.better.abac.jpa.repo.PluginStateRepository;
import care.better.abac.plugin.PluginManager.Key;
import lombok.NonNull;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Andrej Dolenc
 */
public class PluginStateManager {

    private final PluginStateRepository pluginRepository;

    public PluginStateManager(@NonNull PluginStateRepository pluginRepository) {
        this.pluginRepository = pluginRepository;
    }

    @Transactional
    public PluginStateDto savePluginState(PluginStateDto newState) {
        Key key = Key.of(newState.getServiceId(), newState.getPluginId());
        PluginStateDto state = getPluginState(key.getServiceId(), key.getPluginId());
        state.setSyncTime(newState.getSyncTime());
        state.setInitialized(newState.isInitialized());
        save(state);
        return state;
    }

    @Transactional
    public PluginStateDto getPluginState(String pluginId, String serviceId) {
        return getPluginState(Key.of(pluginId, serviceId));
    }

    @Transactional
    public PluginStateDto getPluginState(Key key) {
        return findOrCreatePluginState(key);
    }

    private PluginStateDto findOrCreatePluginState(Key key) {
        return map(Optional.ofNullable(pluginRepository.findByPluginIdAndServiceId(key.getPluginId(), key.getServiceId())).orElseGet(() -> {
            PluginState ps = new PluginState();
            ps.setPluginId(key.getPluginId());
            ps.setServiceId(key.getServiceId());
            ps.setInitialized(false);
            pluginRepository.save(ps);
            return ps;
        }));
    }

    private PluginStateDto map(PluginState ps) {
        return new PluginStateDto(ps.getId(), ps.getPluginId(), ps.getServiceId(), ps.isInitialized(), ps.getSyncTime());
    }

    private PluginState save(PluginStateDto ps) {
        PluginState state = pluginRepository.findByPluginIdAndServiceId(ps.getPluginId(), ps.getServiceId());
        state.setSyncTime(ps.getSyncTime());
        state.setInitialized(ps.isInitialized());
        return pluginRepository.save(state);
    }

}
