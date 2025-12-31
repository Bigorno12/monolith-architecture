package mu.server.rest.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import mu.server.persistence.repository.TokenRepository;
import mu.server.service.service.JwtService;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final TokenRepository tokenRepository;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        try {
            final String authorizationHeader = request.getHeader("Authorization");

            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                final String jwt =  authorizationHeader.substring(7);
                final String username = jwtService.extractUsername(jwt);

                Optional.ofNullable(username)
                        .filter(_ -> SecurityContextHolder.getContext().getAuthentication() == null)
                        .map(userDetailsService::loadUserByUsername)
                        .ifPresent(userDetails -> {
                            if (isTokenValid(jwt) && jwtService.isTokenValid(jwt, userDetails)) {
                                var authToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                                SecurityContext context = SecurityContextHolder.createEmptyContext();
                                context.setAuthentication(authToken);
                                SecurityContextHolder.setContext(context);
                            }
                        });
            }
        } catch (Exception e) {
            logger.error("JWT Authentication Error", e);
        }

        filterChain.doFilter(request, response);
    }

    private Boolean isTokenValid(String token) {
        return tokenRepository.findByToken(token)
                .map(t -> !t.getExpired() && !t.getRevoked())
                .orElse(false);
    }
}
