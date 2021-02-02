package care.better.abac.oauth;

import care.better.abac.BaseWebSecurityConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

/**
 * @author Bostjan Lah
 */
@Configuration
@Import({SsoConfiguration.class, BaseWebSecurityConfiguration.class})
@ConditionalOnProperty(name = "sso.enabled", havingValue = "true")
@Order(1)
public class OAuth2Configuration extends WebSecurityConfigurerAdapter {

    @Autowired
    private SsoConfiguration sso;

    @Autowired
    private Converter<Jwt, AbstractAuthenticationToken> converter;

    @Autowired(required = false)
    private GrantedAuthoritiesMapper grantedAuthoritiesMapper;

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http
                .cors().and()
                .authorizeRequests()
                .antMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .antMatchers("/rest/v1/admin/**").hasRole("ADMIN")
                .antMatchers("/rest/v1/policy/**").permitAll()
                .antMatchers("/rest/v1/**").authenticated()
                .and().oauth2ResourceServer().jwt().jwtAuthenticationConverter(converter);
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        if (sso.getIssuer() != null) {
            return JwtDecoders.fromIssuerLocation(sso.getIssuer());
        } else if (sso.getJwksEndpoint() != null) {
            return NimbusJwtDecoder.withJwkSetUri(sso.getJwksEndpoint()).build();
        } else {
            throw new IllegalArgumentException("At least one of 'sso.issuer' or 'sso.jwksEndpoint' must be set!");
        }
    }

    @Bean
    public SecurityHelper securityHelper() {
        return new SecurityHelper(grantedAuthoritiesMapper);
    }
}
