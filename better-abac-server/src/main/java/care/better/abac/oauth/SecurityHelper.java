package care.better.abac.oauth;

import care.better.core.Opt;
import lombok.NonNull;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * @author Andrej Dolenc
 */
public class SecurityHelper {

    private final GrantedAuthoritiesMapper grantedAuthoritiesMapper;

    public SecurityHelper(GrantedAuthoritiesMapper grantedAuthoritiesMapper) {
        this.grantedAuthoritiesMapper = grantedAuthoritiesMapper;
    }

    public boolean hasAnyRole(@NonNull String... roles) {
        Collection<? extends GrantedAuthority> authorities = Arrays.stream(roles).map(SimpleGrantedAuthority::new).collect(Collectors.toSet());
        Collection<? extends GrantedAuthority> mappedAuthorities = grantedAuthoritiesMapper != null ? grantedAuthoritiesMapper.mapAuthorities(
                authorities) : authorities;
        return Opt.resolve(() -> SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> mappedAuthorities.stream()
                        .map(GrantedAuthority::getAuthority)
                        .anyMatch(a::equals))
        ).orElse(false);
    }

    public boolean isAuthenticated() {
        return Opt.resolve(() -> !SecurityContextHolder.getContext().getAuthentication().getAuthorities().isEmpty()).orElse(false);
    }
}
