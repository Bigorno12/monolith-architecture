package mu.server.service.service;

import mu.server.service.dto.auth.TokenResponse;
import mu.server.service.dto.user.UserRequest;

public interface KeycloakService {

    TokenResponse register(UserRequest request);
}
