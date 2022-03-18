package care.better.abac.policy.execute;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/**
 * @author Primoz Delopst
 */
public class OAuthTokenAttributeExtractor implements BiFunction<Authentication, String, Object> {

    @Override
    public Object apply(Authentication authentication, String path) {
        if (!(authentication.getPrincipal() instanceof OAuth2AuthenticatedPrincipal)) {
            return null;
        }

        OAuth2AuthenticatedPrincipal principal = (OAuth2AuthenticatedPrincipal)authentication.getPrincipal();

        Map<String, Object> attributes = principal.getAttributes();

        if (path.contains("/")) {
            return getForPath(attributes, Arrays.stream(path.split("/")).collect(Collectors.toList()));
        } else {
            return attributes.get(path);
        }
    }

    @SuppressWarnings("unchecked")
    private Object getForPath(Map<String, Object> claims, List<String> paths) {
        if (paths.isEmpty()) {
            return null;
        }

        String path = paths.iterator().next();
        if (paths.size() == 1) {
            return claims.get(path);
        } else {
            return Optional.ofNullable(claims.get(path))
                    .filter(it -> it instanceof Map<?, ?>)
                    .map(it -> getForPath((Map<String, Object>)it, paths.subList(1, paths.size())))
                    .orElse(null);
        }
    }
}