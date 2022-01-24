package care.better.abac.oauth;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author Andrej Dolenc
 */
@Component
@ConfigurationProperties(prefix = "client.auth")
public class ClientAuthConfiguration {
    @Getter
    @Setter
    private String type;
}
