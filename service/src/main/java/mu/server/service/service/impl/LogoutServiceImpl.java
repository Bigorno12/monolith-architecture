package mu.server.service.service.impl;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import mu.server.persistence.repository.TokenRepository;
import mu.server.service.service.LogoutService;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LogoutServiceImpl implements LogoutService {
    private final TokenRepository tokenRepository;

    @Override
    @Transactional
    public void logout(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, Authentication authentication) {
        final String authHeader = request.getHeader("Authorization");
        final String jwt;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            jwt = authHeader.substring(7);
            tokenRepository.findByToken(jwt).ifPresent(token -> {
                token.setExpired(true);
                token.setRevoked(true);
                tokenRepository.save(token);
                SecurityContextHolder.clearContext();
            });
        }
    }
}
