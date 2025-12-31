package mu.server.rest.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mu.server.service.util.FingerprintUtil;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class FingerprintFilter extends OncePerRequestFilter {
    private static final String FINGERPRINT_HEADER = "X-Fingerprint";

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
        String fingerPrint = FingerprintUtil.generateFingerprint(request);
        request.setAttribute("fingerPrint", fingerPrint);

        response.setHeader(FINGERPRINT_HEADER, fingerPrint);

        String clientFingerprint = request.getHeader(FINGERPRINT_HEADER);
        if (clientFingerprint != null && !clientFingerprint.equals(fingerPrint)) {
            log.warn("Fingerprint mismatch for URI: {} - Client: {}, Server: {}", request.getRequestURI(), clientFingerprint, fingerPrint);
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        filterChain.doFilter(request, response);
    }
}
