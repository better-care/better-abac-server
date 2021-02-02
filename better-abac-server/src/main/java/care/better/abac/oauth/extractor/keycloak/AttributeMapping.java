package care.better.abac.oauth.extractor.keycloak;

/**
 * @author Dusan Markovic
 */
public class AttributeMapping {
    private String tokenAttributePath;
    private String contextKey;

    public String getTokenAttributePath() {
        return tokenAttributePath;
    }

    public void setTokenAttributePath(String tokenAttributePath) {
        this.tokenAttributePath = tokenAttributePath;
    }

    public String getContextKey() {
        return contextKey;
    }

    public void setContextKey(String contextKey) {
        this.contextKey = contextKey;
    }
}
