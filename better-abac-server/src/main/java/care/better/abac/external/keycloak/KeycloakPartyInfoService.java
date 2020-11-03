package care.better.abac.external.keycloak;

import com.fasterxml.jackson.databind.JsonNode;
import care.better.abac.external.AbstractRestPartyInfoService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;

import java.util.Set;
import java.util.UUID;

/**
 * @author Bostjan Lah
 */
public class KeycloakPartyInfoService extends AbstractRestPartyInfoService {
    private static final Logger log = LogManager.getLogger(KeycloakPartyInfoService.class.getName());

    private final String userInfoUrl;

    public KeycloakPartyInfoService(String userInfoUrl) {
        this.userInfoUrl = userInfoUrl;
    }

    @Override
    @Cacheable(value = "users", keyGenerator = "setKeyGenerator", unless = "#result == null")
    public String getFullName(Set<String> externalIds) {
        return super.getFullName(externalIds);
    }

    @Override
    public String resolveExternalId(String externalId) {
        try {
            String url = userInfoUrl + (isUUID(externalId) ? "/users/{id}" : "/users?username={username}");
            ResponseEntity<JsonNode> responseEntity = getRestTemplate().exchange(
                    url,
                    HttpMethod.GET,
                    getHttpEntity(),
                    JsonNode.class,
                    externalId);
            JsonNode node = responseEntity.getBody();
            if (node != null) {
                if (node.isArray()) {
                    node = node.path(0);
                }
                JsonNode firstName = node.path("firstName");
                JsonNode lastName = node.path("lastName");
                return formatFullName(firstName, lastName);
            }
        } catch (RestClientException e) {
            log.warn("Unable to retrieve user info {}!", e.getMessage());
        }
        return null;
    }

    private boolean isUUID(String uuid) {
        try {
            UUID.fromString(uuid);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
