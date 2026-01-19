package mu.server.service.service;

import mu.server.service.dto.user.UpdateUserRequest;
import mu.server.service.dto.user.UserResponse;

public interface UserService {
    UserResponse findUserById(Long id);

    void updateUser(UpdateUserRequest updateUserRequest, String username);
}
