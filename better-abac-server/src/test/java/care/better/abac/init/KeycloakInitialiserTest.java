package care.better.abac.init;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableMap;
import care.better.abac.KeycloakTest;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KeycloakInitialiserTest extends KeycloakTest {
    private static final String BASE_URL = "http://thinkehr2.marand.si:8080/auth/admin/realms/Think!EHR";

    private static final String THINKABAC_CLIENT_ID = "thinkabac";
    private static final String THINKEHR_REST_CLIENT = "thinkehr-rest";

    private static final String USERNAME = "admin";
    private static final String PASSWORD = "1admin3";
    private static final String CLIENT_ID = "realm-management";

    private static final String THINKEHR_DOMAIN_MAPPER = "thinkehr_domain";
    private static final String DEFAULT_DOMAIN = "bostjanl";

    private final RestTemplate restTemplate = new RestTemplate();
    private final Map<String, String> clientNameToId = new HashMap<>();

    @Test
    @Ignore
    public void initClient() throws IOException {
        ResponseEntity<JsonNode> responseEntity = restTemplate.exchange(BASE_URL + "/clients", HttpMethod.GET, getRequestEntity(), JsonNode.class);
        for (JsonNode jsonNode : responseEntity.getBody()) {
            clientNameToId.put(jsonNode.path("clientId").textValue(), jsonNode.path("id").textValue());
        }

        if (!clientNameToId.containsKey(THINKABAC_CLIENT_ID)) {
            createThinkAbacClient();
        }
        if (!clientNameToId.containsKey(THINKEHR_REST_CLIENT)) {
            createThinkEhrRestClient();
        }
    }

    @Override
    protected String getPassword() {
        return PASSWORD;
    }

    @Override
    protected String getUsername() {
        return USERNAME;
    }

    @Override
    protected String getClientId() {
        return CLIENT_ID;
    }

    @Override
    protected void addLoginHeaders(HttpHeaders httpHeaders) {
        httpHeaders.set(HttpHeaders.AUTHORIZATION,
                        "Basic " + Base64.getEncoder().encodeToString((CLIENT_ID + ':' + "febade1e-e4a1-4ea6-b5ca-163a365c93a1").getBytes()));
    }

    private void createThinkAbacClient() {
        String id = createClient(THINKABAC_CLIENT_ID, "Think!ABAC Client");
        createClientRole(id, "ADMIN");
        createClientRole(id, "POLICY");

        String userId = createUser(id, "abacadmin", "1admin3", "Admin", "User");
        addClientRoles(userId, THINKABAC_CLIENT_ID, "ADMIN", "POLICY");
        addClientRoles(userId, "realm-management", "view-users");
    }

    private void createThinkEhrRestClient() {
        String id = createClient(THINKEHR_REST_CLIENT, "Think!EHR Client");

        for (String role : new String[]{"ROLE_ADMIN", "ROLE_RO_USER", "ROLE_POPULATION_QUERY_USER", "ROLE_RW_USER", "ROLE_TEMPLATE_MANAGER", "ROLE_USER_MANAGER", "ROLE_EVENT_MANAGER", "ROLE_REGISTERED_QUERY_MANAGER"}) {
            createClientRole(id, role);
        }

        String userId1 = createUser(id, "john", "john", "John", "Smith");
        addClientRoles(userId1, THINKEHR_REST_CLIENT, "ROLE_ADMIN");
        String userId2 = createUser(id, "amy", "amy", "Amy", "Jones");
        addClientRoles(userId2, THINKEHR_REST_CLIENT, "ROLE_ADMIN");
        String userId3 = createUser(id, "alison", "alison", "Alison", "Taylor");
        addClientRoles(userId3, THINKEHR_REST_CLIENT, "ROLE_ADMIN");
        String userId4 = createUser(id, "mary", "mary", "Mary", "Williams");
        addClientRoles(userId4, THINKEHR_REST_CLIENT, "ROLE_ADMIN");

        Map<String, Object> mapper = ImmutableMap.of(
                "config", ImmutableMap.of(
                        "access.token.claim", true,
                        "jsonType.label", "String",
                        "user.attribute", THINKEHR_DOMAIN_MAPPER
                ),
                "name", THINKEHR_DOMAIN_MAPPER,
                "protocol", "openid-connect",
                "protocolMapper", "oidc-usermodel-attribute-mapper"
        );
        restTemplate.postForLocation(BASE_URL + "/clients/{clientId}/protocol-mappers/models", getRequestEntity(mapper), id);
    }

    private String createUser(String id, String username, String password, String firstName, String lastName) {
        ResponseEntity<JsonNode> hitsResponse = restTemplate.exchange(UriComponentsBuilder.fromHttpUrl(BASE_URL + "/users")
                                                                              .queryParam("username", username)
                                                                              .build().toUri(),
                                                                      HttpMethod.GET, getRequestEntity(), JsonNode.class);
        for (JsonNode userNode : hitsResponse.getBody()) {
            restTemplate.exchange(BASE_URL + "/users/{id}", HttpMethod.DELETE, getRequestEntity(), JsonNode.class, userNode.path("id").textValue());
        }

        Map<String, Object> userData = new HashMap<>();
        userData.put("username", username);
        userData.put("enabled", true);
        userData.put("emailVerified", true);
        userData.put("firstName", firstName);
        userData.put("lastName", lastName);
        userData.put("attributes", ImmutableMap.of(THINKEHR_DOMAIN_MAPPER, new String[]{DEFAULT_DOMAIN}));

        URI userUri = restTemplate.postForLocation(BASE_URL + "/users", getRequestEntity(userData), id);
        String userId = getId(userUri);

        Map<String, Object> passwordData = new HashMap<>();
        passwordData.put("type", "password");
        passwordData.put("value", password);
        passwordData.put("temporary", false);
        restTemplate.put(BASE_URL + "/users/{id}/reset-password", getRequestEntity(passwordData), userId);

        return userId;
    }

    private void addClientRoles(String userId, String clientName, String... roles) {
        ResponseEntity<JsonNode> exchange = restTemplate.exchange(
                BASE_URL + "/users/{userId}/role-mappings/clients/{clientId}/available", HttpMethod.GET, getRequestEntity(),
                JsonNode.class, userId, clientNameToId.get(clientName));
        List<Map<String, Object>> roleDtos = new ArrayList<>();
        for (String role : roles) {
            for (JsonNode roleNode : exchange.getBody()) {
                String roleName = roleNode.path("name").textValue();
                if (role.equals(roleName)) {
                    roleDtos.add(ImmutableMap.of("id", roleNode.path("id").textValue(), "name", roleName));
                }
            }
        }

        restTemplate.postForLocation(BASE_URL + "/users/{userId}/role-mappings/clients/{clientId}", getRequestEntity(roleDtos), userId,
                                     clientNameToId.get(clientName));
    }

    private URI createClientRole(String id, String roleName) {
        return restTemplate.postForLocation(BASE_URL + "/clients/{id}/roles", getRequestEntity(ImmutableMap.of("name", roleName)), id);
    }

    private String createClient(String clientId, String clientName) {
        Map<String, Object> clientData = new HashMap<>();
        clientData.put("clientId", clientId);
        clientData.put("name", clientName);
        clientData.put("enabled", true);
        clientData.put("redirectUris", new String[]{"http://localhost:8080/*"});
        clientData.put("webOrigins", new String[]{"*"});
        clientData.put("consentRequired", false);
        clientData.put("publicClient", true);
        clientData.put("directAccessGrantsEnabled", true);

        URI uri = restTemplate.postForLocation(BASE_URL + "/clients", getRequestEntity(clientData));
        String id = getId(uri);
        clientNameToId.put(clientId, id);
        return id;
    }
}
