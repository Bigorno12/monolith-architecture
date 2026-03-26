package mu.server.service.service.impl

import mu.server.persistence.entity.User
import mu.server.persistence.repository.UserRepository
import mu.server.service.dto.Result
import mu.server.service.dto.user.UpdateUserRequest
import mu.server.service.dto.user.UserResponse
import mu.server.service.exception.NoFoundException
import mu.server.service.mapper.UserMapper
import mu.server.service.service.UserService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Service
class UserServiceImpl(private val userRepository: UserRepository, private val userMapper: UserMapper) : UserService {

    companion object {
        private val LOG = LoggerFactory.getLogger(UserServiceImpl::class.java)
    }

    @Transactional(readOnly = true)
    override fun findUserById(id: Long): Result<UserResponse>? = userRepository.findById(id)
        .map { userMapper.mapToUserResponse(it) }
        .map { Result.ok(it) }
        .orElse(Result.failure("User Not Found $id"))

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    override fun updateUser(
        updateUserRequest: UpdateUserRequest,
        username: String
    ): UpdateUserRequest {
        val user: User = userRepository.findByUsername(username) ?: throw NoFoundException("User $username not found")

        if (user.username.equals(updateUserRequest.username, ignoreCase = true)) {
            userRepository.findUserByUsername(user.username)
                ?: throw NoFoundException("Username $username already exists!!")
        }

        userRepository.save(userMapper.updateUserFromDto(updateUserRequest, user))
        return userMapper.mapToUpdateUser(user)
    }
}