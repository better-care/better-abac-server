package care.better.abac.plugin.auth;

/**
 * @author Andrej Dolenc
 */
@FunctionalInterface
public interface AuthorizationProvider {
    String getAuthorization();
}
