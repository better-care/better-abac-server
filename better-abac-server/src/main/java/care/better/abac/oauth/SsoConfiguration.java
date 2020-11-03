package care.better.abac.oauth;

import care.better.abac.oauth.extractor.Extractors;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Andrej Dolenc
 */
@Component
@ConfigurationProperties(prefix = "sso")
@ComponentScan(basePackageClasses = Extractors.class)
public class SsoConfiguration {

    @Getter
    @Setter
    private boolean enabled;

    @Getter
    @Setter
    private String rolesClientId;

    @Getter
    @Setter
    private String issuer;

    @Getter
    @Setter
    private String jwksEndpoint;

    @Getter
    @Setter
    private String tokenDataExtractor;

    @Getter
    @Setter
    private List<String> rolesPath = new LinkedList<>();
}
