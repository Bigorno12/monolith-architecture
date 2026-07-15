package mu.server.service.service

import mu.server.service.dto.Result
import mu.server.service.dto.user.UpdateUserRequest
import mu.server.service.dto.user.UserResponse
import mu.server.service.dto.user.ViewUserProfile

interface UserService {
    fun findUserById(id: Long): Result<UserResponse>?

    fun updateUser(
        updateUserRequest: UpdateUserRequest,
        username: String,
    ): UpdateUserRequest

    fun deleteUser(username: String)

    fun viewUserProfile(username: String): ViewUserProfile
}
