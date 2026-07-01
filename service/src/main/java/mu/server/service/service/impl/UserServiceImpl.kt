package mu.server.service.service.impl

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker
import jakarta.ws.rs.WebApplicationException
import jakarta.ws.rs.core.Response
import mu.server.persistence.entity.User
import mu.server.persistence.repository.UserRepository
import mu.server.service.dto.Result
import mu.server.service.dto.user.UpdateUserRequest
import mu.server.service.dto.user.UserResponse
import mu.server.service.exception.KeycloakException
import mu.server.service.exception.NotFoundException
import mu.server.service.mapper.user.UserMapper
import mu.server.service.service.UserService
import org.keycloak.admin.client.resource.UsersResource
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Service
class UserServiceImpl(
    private val userRepository: UserRepository,
    private val userMapper: UserMapper,
    private val usersResource: UsersResource
) : UserService {

    companion object {
        private val LOG = LoggerFactory.getLogger(UserServiceImpl::class.java)
    }

    @Transactional(readOnly = true)
    override fun findUserById(id: Long): Result<UserResponse>? = userRepository.findById(id)
        .map { userMapper.mapToUserResponse(it) }
        .map { Result.ok(it) }
        .orElse(Result.failure("User Not Found $id"))

    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = [NotFoundException::class])
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

    @CircuitBreaker(name = "keycloakService", fallbackMethod = "fallbackDeleteUser")
    @Transactional(rollbackFor = [NotFoundException::class, KeycloakException::class])
    override fun deleteUser(username: String) {
        val user: User = userRepository.findUserByUsername(username)
            .orElseThrow { NotFoundException("User $username not found") }

        try {
            userRepository.deleteById(user.id)
            usersResource.delete(user.keycloakId).use { response ->
                if (Response.Status.NOT_FOUND.statusCode == response.status) {
                    throw NotFoundException("Username not found in Keycloak with ID: $username")
                }
                if (Response.Status.Family.SUCCESSFUL != response.statusInfo.family) {
                    LOG.error("Failed to delete Keycloak user. Status: {}", response.status)
                    throw KeycloakException("KeycloakId does not exist")
                }
                LOG.info("Successfully deleted user with keycloak ID: {}", user.keycloakId)
            }
        } catch (e: WebApplicationException) {
            LOG.error("Keycloak API Error during user deletion", e)
            throw KeycloakException("Keycloak API Error")
        }
    }

    fun fallbackDeleteUser(username: String, ex: Throwable) {
        LOG.error(
            "Circuit breaker triggered for deleteUser with username: {} and error message: {}",
            username,
            ex.message
        )
    }
}