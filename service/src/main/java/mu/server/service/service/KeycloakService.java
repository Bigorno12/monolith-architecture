package mu.server.service.service;

import jakarta.servlet.http.HttpServletRequest;
import mu.server.service.dto.auth.TokenResponse;
import mu.server.service.dto.user.UserRequest;

public interface KeycloakService {

    TokenResponse register(UserRequest userRequest, HttpServletRequest request);

    TokenResponse authenticate(String username, String password, HttpServletRequest request);
}
