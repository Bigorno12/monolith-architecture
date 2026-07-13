package mu.server.service.service.impl;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mu.server.persistence.entity.User;
import mu.server.persistence.repository.UserRepository;
import mu.server.service.dto.auth.TokenResponse;
import mu.server.service.dto.user.UserRequest;
import mu.server.service.mapper.user.UserMapper;
import mu.server.service.service.KeycloakService;
import mu.server.service.service.KeycloakTokenProvider;
import mu.server.service.util.Credentials;
import mu.server.service.util.FingerprintUtil;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class KeycloakServiceImpl implements KeycloakService {

    private final UsersResource usersResource;
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final KeycloakTokenProvider keycloakTokenProvider;
    private final CacheManager cacheManager;

    @Override
    @Transactional
    public TokenResponse register(UserRequest userRequest, HttpServletRequest request) {
        CredentialRepresentation credentialRepresentation = Credentials.INSTANCE.createCredentialRepresentation(userRequest.password());
        UserRepresentation userRepresentation = userMapper.mapToUserRepresentation(userRequest, credentialRepresentation);

        try (Response response = usersResource.create(userRepresentation)) {
            if (response.getStatus() != 201) {
                String errorResponse = response.hasEntity() ? response.readEntity(String.class) : "No explicit details provided";
                log.error("Keycloak registration failed! Status: {}, Details: {}", response.getStatus(), errorResponse);

                throw new RuntimeException("Failed to create user in Keycloak. Status: " + response.getStatus() + " Details: " + errorResponse);
            }

            String keycloakId = response.getLocation().getPath()
                    .substring(response.getLocation().getPath().lastIndexOf("/") + 1);

            RoleRepresentation roleRepresentation = usersResource.get(keycloakId)
                    .roles()
                    .realmLevel()
                    .listAvailable()
                    .stream()
                    .filter(role -> role.getName().equals(userRequest.role().name()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Role not found: " + userRequest.role().name()));

            usersResource.get(keycloakId).roles().realmLevel().add(List.of(roleRepresentation));

            User user = userMapper.mapToUser(userRequest);
            user.setKeycloakId(keycloakId);

            userRepository.save(user);
            log.info("Username {} successfully created with keycloakId: {}", user.getUsername(), keycloakId);

            return authenticate(userRequest.username(), userRequest.password(), request);
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
}