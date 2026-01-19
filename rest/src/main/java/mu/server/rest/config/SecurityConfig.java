package mu.server.rest.config;

import lombok.RequiredArgsConstructor;
import mu.server.rest.filter.FingerprintFilter;
import mu.server.rest.filter.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutHandler;
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
            "/jsonplaceholder.typicode.com/**"
    };
    private static final String path = "/api/v1/mono/admin/**";
    private static final String userPath = "/api/v1/mono/**";

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final FingerprintFilter fingerprintFilter;
    private final AuthenticationProvider authenticationProvider;
    private final LogoutHandler logoutHandler;

    @Bean
    public SecurityFilterChain configure(HttpSecurity http) {
        return http.csrf(AbstractHttpConfigurer::disable)
                .cors(httpSecurityCorsConfigurer -> httpSecurityCorsConfigurer.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(auth -> auth.requestMatchers(WHITELISTED_PATHS)
                        .permitAll()
                        .requestMatchers(GET, path).hasAnyAuthority(ADMIN_READ.getPermission())
                        .requestMatchers(POST, path).hasAnyAuthority(ADMIN_CREATE.getPermission())
                        .requestMatchers(PUT, path).hasAnyAuthority(ADMIN_UPDATE.getPermission())
                        .requestMatchers(DELETE, path).hasAnyAuthority(ADMIN_DELETE.getPermission())
                        .requestMatchers(PUT, userPath).hasAnyAuthority(USER_UPDATE.getPermission())
                        .requestMatchers(POST, userPath).hasAnyAuthority(USER_CREATE.getPermission())
                        .requestMatchers(GET, userPath).hasAnyAuthority(USER_READ.getPermission())
                        .requestMatchers(DELETE, userPath).hasAnyAuthority(USER_DELETE.getPermission())
                        .anyRequest()
                        .authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(STATELESS))
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(fingerprintFilter, JwtAuthenticationFilter.class)
                .exceptionHandling(exception -> exception.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .httpBasic(Customizer.withDefaults())
                .logout(logout -> logout.logoutUrl("/api/v1/auth/logout")
                        .addLogoutHandler(logoutHandler)
                        .invalidateHttpSession(true)
                        .logoutSuccessHandler((_, _, _) -> {
                            SecurityContextHolder.clearContext();
                            new HttpStatusReturningLogoutSuccessHandler(HttpStatus.OK);
                        })
                        .deleteCookies("JSESSIONID", "XSRF-TOKEN")
                )
                .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.setAllowedOrigins(List.of("http://localhost:8080"));
        corsConfiguration.setAllowedMethods(List.of("*"));
        corsConfiguration.setAllowedHeaders(List.of("*"));
        corsConfiguration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfiguration);
        return source;
    }
}