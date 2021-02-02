package care.better.abac.oauth.extractor.keycloak;

import com.google.common.collect.Sets;
import care.better.core.Opt;
import care.better.abac.oauth.SsoConfiguration;
import lombok.NonNull;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.oauth2.core.oidc.StandardClaimNames;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Andrej Dolenc
 */
public class KeycloakGrantedAuthoritiesConverter implements Converter<Jwt, Collection<GrantedAuthority>> {
    private static final Logger log = LogManager.getLogger(KeycloakGrantedAuthoritiesConverter.class);

    private static final String ROLE_PATH_DELIMITER = "/";
    private static final String RESOURCE_ACCESS_ATTRIBUTE = "resource_access";
    private static final String ROLES_ATTRIBUTE = "roles";

    private final Set<String> rolePaths;
    private final GrantedAuthoritiesMapper authoritiesMapper;

    public KeycloakGrantedAuthoritiesConverter(@NonNull SsoConfiguration ssoConfiguration, GrantedAuthoritiesMapper authoritiesMapper) {
        rolePaths = Sets.newHashSet(ssoConfiguration.getRolesPath());
        rolePaths.add(String.join(ROLE_PATH_DELIMITER, RESOURCE_ACCESS_ATTRIBUTE, ssoConfiguration.getRolesClientId(), ROLES_ATTRIBUTE));
        this.authoritiesMapper = authoritiesMapper;
    }

    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        Map<String, Object> claims = jwt.getClaims();

        Set<String> accessByIdRoles = rolePaths.stream()
                .map(p -> extractRoles(p, claims))
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());

        String username = (String)claims.get(StandardClaimNames.PREFERRED_USERNAME);
        log.debug("Username: {}, roles: {}", username, accessByIdRoles);
        Set<GrantedAuthority> authorities = accessByIdRoles.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toSet());
        return authoritiesMapper != null ? authoritiesMapper.mapAuthorities(authorities).stream().collect(Collectors.toSet()) : authorities;

    }

    private List<String> extractRoles(String rolePath, Map<String, Object> claims) {
        String[] paths = StringUtils.tokenizeToStringArray(rolePath, "/", true, true);
        Map<String, Object> currentNode = claims;
        String nextPath = rolePath;
        if (paths.length > 0) {
            for (int i = 0; i < paths.length - 1; i++) {
                currentNode = getNode(currentNode, paths[i]);
                nextPath = paths[i+1];
                if (currentNode == null) {
                    break;
                }
            }
        }
        return getValue(currentNode, nextPath);
    }

    private Map<String, Object> getNode(Map<String, Object> claims, String path) {
        return Opt.resolve(() -> (Map<String, Object>)claims.get(path)).orElse(null);
    }

    private List<String> getValue(Map<String, Object> claims, String path) {
        return Opt.resolve(() -> (List<String>)claims.get(path)).orElse(Collections.emptyList());
    }
}
