package mu.server.rest.config;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;

@Slf4j
@Configuration
public class KeycloakAuthenticationConverter {

    @Bean
    public JwtAuthenticationConverter authenticationConverter(AuthoritiesConverter converter) {
        var authenticateConverter = new JwtAuthenticationConverter();
        authenticateConverter.setJwtGrantedAuthoritiesConverter(jwt -> converter.convert(jwt.getClaims()));
        authenticateConverter.setPrincipalClaimName("preferred_username");
        return authenticateConverter;
    }

    @Bean
    public GrantedAuthoritiesMapper grantedAuthoritiesMapper(AuthoritiesConverter authoritiesConverter) {
        return authorities -> authorities.stream()
                .filter(authority -> authority instanceof OidcUserAuthority)
                .map(OidcUserAuthority.class::cast)
                .map(OidcUserAuthority::getIdToken)
                .map(OidcIdToken::getClaims)
                .map(authoritiesConverter::convert)
                .flatMap(Collection::stream)
                .toList();
    }

    @Bean
    @SuppressWarnings("unchecked")
    public AuthoritiesConverter realmRolesAuthoritiesConverter() {
        return claims -> {
            Optional<Map<String, Object>> realmAccess = Optional.ofNullable((Map<String, Object>) claims.get("realm_access"));
            Optional<List<String>> roles = realmAccess.flatMap(role -> Optional.ofNullable((List<String>) role.get("roles")));
            List<GrantedAuthority> grantedAuthorities = roles.stream()
                    .flatMap(Collection::stream)
                    .map(SimpleGrantedAuthority::new)
                    .map(GrantedAuthority.class::cast)
                    .toList();

            log.info("Granted Authorities: {}", grantedAuthorities);

            return grantedAuthorities;
        };
    }
}
