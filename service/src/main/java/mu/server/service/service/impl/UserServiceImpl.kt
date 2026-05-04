package mu.server.service.service.impl

import mu.server.persistence.repository.UserRepository
import mu.server.service.dto.Result
import mu.server.service.dto.user.UpdateUserRequest
import mu.server.service.dto.user.UserResponse
import mu.server.service.exception.NotFoundException
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
        return userRepository.findByUsername(username)
            .also {
                if (it.username == updateUserRequest.username())
                    userRepository.findUserByUsername(it.username) ?: throw NotFoundException("User $username found")

            }
            .let { userMapper.updateUserFromDto(updateUserRequest, it) }
            .let { userMapper.mapToUpdateUser(it) } ?: throw NotFoundException("User $username not found")
    }
}