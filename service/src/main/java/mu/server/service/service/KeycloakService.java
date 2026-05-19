package mu.server.service.service;

import mu.server.service.dto.user.UserRequest;

public interface KeycloakService {

    void register(UserRequest request);
}
