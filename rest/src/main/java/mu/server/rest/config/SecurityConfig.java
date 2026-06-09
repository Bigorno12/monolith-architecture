package mu.server.rest.config;

import lombok.RequiredArgsConstructor;
import mu.server.rest.filter.FingerprintFilter;
import mu.server.rest.filter.RateLimitFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

import static mu.server.persistence.enumeration.Permission.ADMIN_CREATE;
import static mu.server.persistence.enumeration.Permission.ADMIN_DELETE;
import static mu.server.persistence.enumeration.Permission.ADMIN_READ;
import static mu.server.persistence.enumeration.Permission.ADMIN_UPDATE;
import static mu.server.persistence.enumeration.Permission.USER_CREATE;
import static mu.server.persistence.enumeration.Permission.USER_DELETE;
import static mu.server.persistence.enumeration.Permission.USER_READ;
import static mu.server.persistence.enumeration.Permission.USER_UPDATE;
import static org.springframework.data.web.config.EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
@EnableSpringDataWebSupport(pageSerializationMode = VIA_DTO)
@EnableMethodSecurity(jsr250Enabled = true, securedEnabled = true)
public class SecurityConfig {

    private static final String[] WHITELISTED_PATHS = {
            "/api/v1/auth/**",
            "/api/v2/auth/**",
            "/error",
            "/v2/api-docs",
            "/v3/api-docs",
            "/v3/api-docs/**",
            "/swagger-resources",
            "/swagger-resources/**",
            "/configuration/ui",
            "/configuration/security",
            "/swagger-ui/**",
            "/webjars/**",
            "/swagger-ui.html",
            "/h2-console/**",
            "/jsonplaceholder.typicode.com/**",
            "/actuator/**",
            "/login/oauth2/code/**"
    };

    private static final String ADMIN_PATH = "/api/v1/mono/admin/**";
    private static final String USER_PATH = "/api/v1/mono/**";

    private final FingerprintFilter fingerprintFilter;
    private final RateLimitFilter rateLimitFilter;

    @Bean
    @Order(1)
    public SecurityFilterChain resourceServerSecurityFilterChain(
            HttpSecurity http,
            JwtAuthenticationConverter jwtAuthenticationConverter) {
        return http
                .securityMatcher("/api/v1/mono/**")
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(STATELESS))
                .addFilterAfter(fingerprintFilter, BearerTokenAuthenticationFilter.class)
                .addFilterAfter(rateLimitFilter, FingerprintFilter.class)
//                .exceptionHandling(ex -> ex.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter))
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(GET, ADMIN_PATH).hasAnyAuthority(ADMIN_READ.getPermission())
                        .requestMatchers(POST, ADMIN_PATH).hasAnyAuthority(ADMIN_CREATE.getPermission())
                        .requestMatchers(PUT, ADMIN_PATH).hasAnyAuthority(ADMIN_UPDATE.getPermission())
                        .requestMatchers(DELETE, ADMIN_PATH).hasAnyAuthority(ADMIN_DELETE.getPermission())
                        .requestMatchers(PUT, USER_PATH).hasAnyAuthority(USER_UPDATE.getPermission(), ADMIN_UPDATE.getPermission())
                        .requestMatchers(POST, USER_PATH).hasAnyAuthority(USER_CREATE.getPermission(), ADMIN_CREATE.getPermission())
                        .requestMatchers(GET, USER_PATH).hasAnyAuthority(USER_READ.getPermission(), ADMIN_READ.getPermission())
                        .requestMatchers(DELETE, USER_PATH).hasAnyAuthority(USER_DELETE.getPermission(), ADMIN_DELETE.getPermission())
                        .anyRequest().authenticated()
                )
                .build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain clientSecurityFilterChain(
            HttpSecurity http,
            ClientRegistrationRepository clientRegistrationRepository) {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable))
                .oauth2Login(Customizer.withDefaults())
                .logout(logout -> {
                    var oidcLogoutHandler = new OidcClientInitiatedLogoutSuccessHandler(clientRegistrationRepository);
                    oidcLogoutHandler.setPostLogoutRedirectUri("{baseUrl}/");

                    logout.logoutUrl("/api/v1/auth/logout")
                            .invalidateHttpSession(true)
                            .logoutSuccessHandler(oidcLogoutHandler)
                            .deleteCookies("JSESSIONID", "XSRF-TOKEN");
                })
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(WHITELISTED_PATHS).permitAll()
                        .anyRequest().authenticated()
                )
                .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:8080"));
        config.setAllowedMethods(List.of("*"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}