package care.better.abac.external;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AccessLevel;
import lombok.Getter;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthentication;
import org.springframework.web.client.RestTemplate;

/**
 * @author Bostjan Lah
 */
public abstract class AbstractRestPartyInfoService extends AbstractPartyInfoService {
    @Getter(AccessLevel.PROTECTED)
    private final RestTemplate restTemplate = new RestTemplate();

    protected HttpEntity<?> getHttpEntity() {
        HttpHeaders headers = new HttpHeaders();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof BearerTokenAuthentication) {
            BearerTokenAuthentication bearerTokenAuthentication = (BearerTokenAuthentication)authentication;
            headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + bearerTokenAuthentication.getToken().getTokenValue());
        }
        return new HttpEntity<>(headers);
    }

    protected String formatFullName(JsonNode firstNames, JsonNode lastNames) {
        StringBuilder builder = new StringBuilder();
        if (firstNames.isTextual()) {
            builder.append(firstNames.textValue());
        }
        if (lastNames.isTextual()) {
            builder.append(' ').append(lastNames.textValue());
        }
        return builder.toString().trim();
    }
}
