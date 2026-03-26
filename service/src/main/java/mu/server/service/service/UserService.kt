package mu.server.service.service;

import mu.server.service.dto.Result;
import mu.server.service.dto.user.UpdateUserRequest;
import mu.server.service.dto.user.UserResponse;

public interface UserService {
    Result<UserResponse> findUserById(Long id);

    UpdateUserRequest updateUser(UpdateUserRequest updateUserRequest, String username);
}
