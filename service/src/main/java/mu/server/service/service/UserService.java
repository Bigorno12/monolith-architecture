package mu.server.service.service;

import mu.server.service.dto.UserResponse;

public interface UserService {
    UserResponse findUserById(Long id);
}
