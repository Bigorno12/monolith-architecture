package mu.server.service.service.impl

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker
import jakarta.ws.rs.WebApplicationException
import jakarta.ws.rs.core.Response
import mu.server.persistence.entity.User
import mu.server.persistence.repository.UserRepository
import mu.server.service.dto.Result
import mu.server.service.dto.user.UpdateUserRequest
import mu.server.service.dto.user.UserResponse
import mu.server.service.dto.user.ViewUserProfile
import mu.server.service.exception.KeycloakException
import mu.server.service.exception.NotFoundException
import mu.server.service.exception.UsernameExistException
import mu.server.service.mapper.user.UserMapper
import mu.server.service.service.UserService
import org.keycloak.admin.client.resource.UserResource
import org.keycloak.admin.client.resource.UsersResource
import org.keycloak.representations.idm.UserRepresentation
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.CachePut
import org.springframework.cache.annotation.Cacheable
import org.springframework.cache.annotation.Caching
import org.springframework.stereotype.Service
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

    @CircuitBreaker(name = "userService", fallbackMethod = "fallbackUpdateUser")
    @Transactional(rollbackFor = [NotFoundException::class, UsernameExistException::class])
    @Caching(
        put = [CachePut(
            cacheNames = ["userCache", "keycloakCache"],
            key = "#updateUserRequest.username",
            unless = "#result == null",
            condition = "#updateUserRequest != null"
        )],
        evict = [CacheEvict(
            cacheNames = ["profileCache"],
            key = "#username",
        )]
    )
    override fun updateUser(
        updateUserRequest: UpdateUserRequest,
        username: String
    ): UpdateUserRequest {
        val checkUsernameExist: User? = userRepository.findUserByUsername(username)
            .orElseThrow { NotFoundException("User $username found") }

        if (updateUserRequest.username().equals(checkUsernameExist?.username)) {
            val updateUser: User = userMapper.updateUserFromDto(updateUserRequest, checkUsernameExist)
            userRepository.save(updateUser)
            updateKeycloakUsername(checkUsernameExist?.keycloakId, updateUserRequest)
            return userMapper.mapToUpdateUser(updateUser)
        } else {
            userRepository.findUserByUsername(updateUserRequest.username())
                .ifPresent { throw UsernameExistException("User $username already exists!") }

            val updateUserDifferentUsername: User? = userMapper.updateUserFromDto(updateUserRequest, checkUsernameExist)
            userRepository.save(updateUserDifferentUsername)
            updateKeycloakUsername(checkUsernameExist?.keycloakId, updateUserRequest)
            return userMapper.mapToUpdateUser(updateUserDifferentUsername)
        }
    }

    @Transactional(readOnly = true)
    @Cacheable(
        cacheNames = ["adminCache"],
        unless = "#result == null",
        condition = "#result != null && #id != null",
        key = "#id"
    )
    override fun findUserById(id: Long): Result<UserResponse>? = userRepository.findById(id)
        .map { userMapper.mapToUserResponse(it) }
        .map { Result.ok(it) }
        .orElse(Result.failure("User Not Found $id"))

    @CircuitBreaker(name = "keycloakService", fallbackMethod = "fallbackDeleteUser")
    @Transactional(rollbackFor = [NotFoundException::class, KeycloakException::class])
    @CacheEvict(
        key = "#username",
        cacheNames = ["userCache", "keycloakCache"],
        condition = "#username != null OR #result != null"
    )
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

    @Transactional(readOnly = true, rollbackFor = [NotFoundException::class])
    @CircuitBreaker(name = "userService", fallbackMethod = "fallbackUpdateUser")
    @Cacheable(
        key = "#username",
        cacheNames = ["profileCache"],
        condition = "#username != null OR #result != null",
        unless = "#result == null"
    )
    override fun viewUserProfile(username: String): ViewUserProfile {
        return userRepository.findUserByUsername(username)
            .map { userMapper.mapToUserProfile(it) }
            .orElseThrow { NotFoundException("User $username not found") }
    }

    fun fallbackDeleteUser(username: String, ex: Throwable) {
        LOG.error(
            "Circuit breaker triggered for deleteUser with username: {} and error message: {}",
            username,
            ex.message
        )
        throw ex
    }

    fun fallbackUpdateUser(updateUserRequest: UpdateUserRequest, username: String, ex: Throwable): UpdateUserRequest {
        LOG.error(
            "Circuit breaker triggered for updateUser: {} with username: {} and error message: {}",
            username,
            updateUserRequest,
            ex.message
        )
        throw ex
    }

    private fun updateKeycloakUsername(keycloakId: String?, updateUserRequest: UpdateUserRequest) {
        try {
            val keycloakUserResource: UserResource? = usersResource.get(keycloakId)
            val userRepresentation: UserRepresentation? = updateUserRequest.let { userMapper.updateUserKeycloak(it) }
            keycloakUserResource?.update(userRepresentation)
        } catch (e: WebApplicationException) {
            LOG.error("Keycloak API Error", e)
            throw KeycloakException("Failed to sync user updates to Keycloak: ${e.response?.status}")
        }
    }
}