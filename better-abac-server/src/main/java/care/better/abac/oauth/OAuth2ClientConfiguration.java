package care.better.abac.oauth;

import care.better.abac.plugin.auth.AuthorizationProvider;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizationContext;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProvider;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.OAuth2AccessToken.TokenType;

import java.util.Optional;

/**
 * @author Andrej Dolenc
 */
@Configuration
@Import(ClientAuthConfiguration.class)
@ConditionalOnProperty(name = "client.auth.type", havingValue = "oauth2")
public class OAuth2ClientConfiguration {

    @Autowired
    private ClientAuthConfiguration clientAuthConfiguration;

    @Bean
    public AuthorizedClientServiceOAuth2AuthorizedClientManager oAuth2AuthorizedClientManager(
            @NonNull ClientRegistrationRepository clientRegistrationRepository,
            @NonNull OAuth2AuthorizedClientService oAuth2AuthorizedClientService) {
        OAuth2AuthorizedClientProvider authorizedClientProvider =
                OAuth2AuthorizedClientProviderBuilder.builder()
                        .refreshToken()
                        .password()
                        .build();
        AuthorizedClientServiceOAuth2AuthorizedClientManager authorizedClientManager = new AuthorizedClientServiceOAuth2AuthorizedClientManager(
                clientRegistrationRepository, oAuth2AuthorizedClientService);
        authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider);
        authorizedClientManager.setContextAttributesMapper(OAuth2AuthorizeRequest::getAttributes);
        return authorizedClientManager;
    }

    @Bean
    public AuthorizationProvider authorizationProvider(@NonNull AuthorizedClientServiceOAuth2AuthorizedClientManager manager) {
        OAuth2AuthorizeRequest request = OAuth2AuthorizeRequest.withClientRegistrationId(clientAuthConfiguration.getType())
                .principal(new AnonymousAuthenticationToken())
                .attribute(OAuth2AuthorizationContext.USERNAME_ATTRIBUTE_NAME, clientAuthConfiguration.getUsername())
                .attribute(OAuth2AuthorizationContext.PASSWORD_ATTRIBUTE_NAME, clientAuthConfiguration.getPassword())
                .build();
        return () -> Optional.ofNullable(manager.authorize(request))
                .map(p -> TokenType.BEARER.getValue() + ' ' + p.getAccessToken().getTokenValue())
                .orElse(null);
    }

    private static final class AnonymousAuthenticationToken extends AbstractAuthenticationToken {
        AnonymousAuthenticationToken() {
            super(null);
        }

        @Override
        public Object getCredentials() {
            return null;
        }

        @Override
        public Object getPrincipal() {
            return "abac";
        }
    }
}
