package mu.server.service.service

import mu.server.service.dto.Result
import mu.server.service.dto.user.UpdateUserRequest
import mu.server.service.dto.user.UserResponse

interface UserService {
    fun findUserById(id: Long): Result<UserResponse>?

    fun updateUser(updateUserRequest: UpdateUserRequest, username: String): UpdateUserRequest
}