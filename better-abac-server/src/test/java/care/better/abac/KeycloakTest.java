package care.better.abac;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

/**
 * @author Bostjan Lah
 */
public abstract class KeycloakTest {
    private static final String TOKEN_ENDPOINT = "http://thinkehr2.marand.si:8080/auth/realms/Think!EHR/protocol/openid-connect/token";

    private static final String GRANT_TYPE = "password";

    private HttpHeaders headers;

    @BeforeEach
    public void init() {
        headers = getHeaders();
    }

    protected String getAccessToken() {
        RestTemplate restTemplate = new RestTemplate();

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("client_id", getClientId());
        map.add("username", getUsername());
        map.add("password", getPassword());
        map.add("grant_type", GRANT_TYPE);

        HttpHeaders httpHeaders = new HttpHeaders();
        addLoginHeaders(httpHeaders);

        ResponseEntity<JsonNode> exchange = restTemplate.exchange(TOKEN_ENDPOINT, HttpMethod.POST, new HttpEntity<Object>(map, httpHeaders), JsonNode.class);
        return exchange.getBody().path("access_token").textValue();
    }

    protected void addLoginHeaders(HttpHeaders httpHeaders) {
    }

    protected abstract String getClientId();

    protected abstract String getPassword();

    protected abstract String getUsername();

    protected HttpHeaders getHeaders() {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set(HttpHeaders.AUTHORIZATION, "Bearer " + getAccessToken());
        return httpHeaders;
    }

    protected String getId(URI uri) {
        String uriString = uri.toString();
        return getId(uriString);
    }

    protected String getId(String uriString) {
        return uriString.substring(uriString.lastIndexOf('/') + 1);
    }

    protected <T> HttpEntity<T> getRequestEntity(T entity) {
        return new HttpEntity<>(entity, headers);
    }

    protected HttpEntity<Void> getRequestEntity() {
        return new HttpEntity<>(headers);
    }
}
