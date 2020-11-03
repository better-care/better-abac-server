package care.better.abac.oauth.extractor.keycloak;

import care.better.abac.oauth.DelegatingJwtBearerTokenAuthenticationConverter;
import care.better.abac.oauth.SsoConfiguration;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.authority.mapping.SimpleAuthorityMapper;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collection;

/**
 * @author Andrej Dolenc
 */
@Configuration
@ConditionalOnProperty(name = "sso.tokenDataExtractor", havingValue = "keycloak")
public class KeycloakConfiguration {

    @Bean
    public Converter<Jwt, AbstractAuthenticationToken> keycloakAuthenticationTokenConverter(
            @NonNull @Qualifier("keycloakGrantedAuthoritiesConverter") Converter<Jwt, Collection<GrantedAuthority>> keycloakGrantedAuthoritiesConverter) {
        return new DelegatingJwtBearerTokenAuthenticationConverter(keycloakGrantedAuthoritiesConverter);
    }

    @Bean
    public Converter<Jwt, Collection<GrantedAuthority>> keycloakGrantedAuthoritiesConverter(
            @NonNull SsoConfiguration ssoConfiguration,
            @NonNull GrantedAuthoritiesMapper mapper) {
        return new KeycloakGrantedAuthoritiesConverter(ssoConfiguration, mapper);
    }

    @Bean
    public GrantedAuthoritiesMapper grantedAuthoritiesMapper() {
        return new SimpleAuthorityMapper();
    }
}
