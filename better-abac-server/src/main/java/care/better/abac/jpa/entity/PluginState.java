package care.better.abac.jpa.entity;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.time.OffsetDateTime;

/**
 * @author Andrej Dolenc
 */
@Entity
public class PluginState {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Getter
    @Setter
    private Long id;
    @Setter
    @Getter
    @Basic(optional = false)
    private @NonNull String pluginId;
    @Basic(optional = false)
    @Getter
    @Setter
    private @NonNull String serviceId;
    @Getter
    @Setter
    private boolean initialized;
    @Getter
    @Setter
    private OffsetDateTime syncTime;
}
