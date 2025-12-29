package mu.server.service.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;

public interface LogoutService extends LogoutHandler {
    @Override
    void logout(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @Nullable Authentication authentication);
}
