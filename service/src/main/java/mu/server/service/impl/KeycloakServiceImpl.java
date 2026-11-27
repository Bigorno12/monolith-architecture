package mu.server.service.impl;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.Response;

import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static mu.server.service.dto.api.Role.USER;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mu.server.persistence.entity.User;
import mu.server.persistence.repository.UserRepository;
import mu.server.service.KeycloakService;
import mu.server.service.KeycloakTokenProvider;
import mu.server.service.dto.auth.TokenResponse;
import mu.server.service.dto.user.UserRequest;
import mu.server.service.exception.KeycloakException;
import mu.server.service.exception.RegistrationRejectedException;
import mu.server.service.exception.UsernameExistException;
import mu.server.service.mapper.user.UserMapper;
import mu.server.service.util.Credentials;
import mu.server.service.util.FingerprintUtil;

@Slf4j
@Service
@RequiredArgsConstructor
public class KeycloakServiceImpl implements KeycloakService {

    private static final String NO_ERROR_DETAILS = "No explicit details provided";

    private final UsersResource usersResource;
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final KeycloakTokenProvider keycloakTokenProvider;
    private final CacheManager cacheManager;

    @Override
    @Transactional
    public TokenResponse register(UserRequest userRequest, HttpServletRequest request) {
        userRepository.findUserByUsername(userRequest.username())
                .ifPresent(user -> {
                    throw new UsernameExistException("Username: " + user.getUsername() + " already exist");
                });

        CredentialRepresentation credentialRepresentation = Credentials.INSTANCE.createCredentialRepresentation(userRequest.password());
        UserRepresentation userRepresentation = userMapper.mapToUserRepresentation(userRequest, credentialRepresentation);
        String keycloakId = createKeycloakUser(userRepresentation);

        try {
            assignUserRole(keycloakId);

            User user = userMapper.mapToUser(userRequest, keycloakId);
            userRepository.saveAndFlush(user);
            log.info("Username {} successfully created with keycloakId: {}", user.getUsername(), keycloakId);

            return authenticate(userRequest.username(), userRequest.password(), request);
        } catch (RuntimeException e) {
            removeKeycloakUser(keycloakId);
            throw e;
        }
    }

    @Override
    public TokenResponse authenticate(String username, String password, HttpServletRequest request) {
        TokenResponse token = keycloakTokenProvider.getToken(username, password);

        Optional.ofNullable(token.getAccessToken())
                .map(_ -> FingerprintUtil.generateFingerprint(request))
                .ifPresent(fingerPrint -> {
                    Cache fingerprintCache = cacheManager.getCache("fingerprintCache");
                    if (fingerprintCache != null) {
                        fingerprintCache.put(token.getAccessToken(), fingerPrint);
                        log.info("Successfully bound keycloak token to device fingerprint for user: {}", username);
                    }
                });

        return token;
    }

    private String createKeycloakUser(UserRepresentation userRepresentation) {
        try (Response response = usersResource.create(userRepresentation)) {
            int status = response.getStatus();
            if (status == Response.Status.CREATED.getStatusCode()) {
                return CreatedResponseUtil.getCreatedId(response);
            }

            String details = readErrorDetails(response);
            log.warn("Keycloak registration failed! Status: {}, Details: {}", status, details);

            if (status == Response.Status.CONFLICT.getStatusCode()) {
                throw new UsernameExistException("Username or email already exist");
            }
            if (status == Response.Status.BAD_REQUEST.getStatusCode()) {
                throw new RegistrationRejectedException("Registration rejected: " + details);
            }
            throw new KeycloakException("Failed to create user in Keycloak. Status: " + status);
        }
    }

    private static String readErrorDetails(Response response) {
        if (!response.hasEntity()) {
            return NO_ERROR_DETAILS;
        }
        try {
            Map<String, Object> body = response.readEntity(new GenericType<>() {});
            return Stream.of("errorMessage", "error_description", "error")
                    .map(body::get)
                    .filter(Objects::nonNull)
                    .map(Object::toString)
                    .findFirst()
                    .orElse(NO_ERROR_DETAILS);
        } catch (ProcessingException e) {
            return NO_ERROR_DETAILS;
        }
    }

    private void assignUserRole(String keycloakId) {
        RoleRepresentation roleRepresentation = usersResource.get(keycloakId)
                .roles()
                .realmLevel()
                .listAvailable()
                .stream()
                .filter(role -> role.getName().equals(USER.name()))
                .findFirst()
                .orElseThrow(() -> new KeycloakException("Role not found: " + USER.name()));

        usersResource.get(keycloakId).roles().realmLevel().add(List.of(roleRepresentation));
    }

    private void removeKeycloakUser(String keycloakId) {
        try (Response response = usersResource.delete(keycloakId)) {
            log.warn("Registration failed; removed Keycloak user {} (status {})", keycloakId, response.getStatus());
        } catch (RuntimeException e) {
            log.error("Registration failed and Keycloak user {} could not be removed; delete it manually", keycloakId, e);
        }
    }
}
