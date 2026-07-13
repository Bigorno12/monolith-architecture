package mu.server.rest.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mu.server.service.util.FingerprintUtil;
import org.jspecify.annotations.NonNull;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
@RequiredArgsConstructor
public class FingerprintFilter extends OncePerRequestFilter {

    private final CacheManager cacheManager;

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        return !request.getRequestURI().startsWith("/api/v1/mono/");
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
        final String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
           filterChain.doFilter(request, response);
           return;
        }

        final String accessToken = authorizationHeader.substring(7);
        Cache fingerprintCache = cacheManager.getCache("fingerprintCache");

        if (fingerprintCache == null) {
            filterChain.doFilter(request, response);
            return;
        }

        String cachedFingerprint = fingerprintCache.get(accessToken, String.class);
        if (cachedFingerprint != null) {
            String actualFingerprint = FingerprintUtil.generateFingerprint(request);
            if (!actualFingerprint.equals(cachedFingerprint)) {
                log.warn("CRITICAL SECURITY ALERT: Hijacked token detected for URI: {}!", request.getRequestURI());
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Session hijacking detected. Access denied.");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}
