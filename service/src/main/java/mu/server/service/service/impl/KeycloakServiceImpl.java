package mu.server.service.service.impl;

import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mu.server.persistence.entity.User;
import mu.server.persistence.repository.UserRepository;
import mu.server.service.dto.auth.TokenResponse;
import mu.server.service.dto.user.UserRequest;
import mu.server.service.mapper.UserMapper;
import mu.server.service.service.KeycloakService;
import mu.server.service.service.KeycloakTokenProvider;
import mu.server.service.util.Credentials;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class KeycloakServiceImpl implements KeycloakService {

    private final UsersResource usersResource;
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final KeycloakTokenProvider keycloakTokenProvider;

    @Override
    @Transactional
    public TokenResponse register(UserRequest request) {
        CredentialRepresentation credentialRepresentation = Credentials.INSTANCE.createCredentialRepresentation(request.password());
        UserRepresentation userRepresentation = userMapper.mapToUserRepresentation(request, credentialRepresentation);

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
                    .filter(role -> role.getName().equals(request.role().name()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Role not found: " + request.role().name()));

            usersResource.get(keycloakId).roles().realmLevel().add(List.of(roleRepresentation));

            User user = userMapper.mapToUser(request);
            user.setKeycloakId(keycloakId);

            userRepository.save(user);
            log.info("Username {} successfully created with keycloakId: {}", user.getUsername(), keycloakId);

            return keycloakTokenProvider.getToken(request.username(), request.password());
        }

    }

}