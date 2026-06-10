package mu.server.service.mapper.user;

import mu.server.service.dto.user.UserRequest;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Collections;

@Mapper(componentModel = "spring", imports = {Collections.class})
public interface KeycloakMapper {
    @Mapping(target = "enabled", constant = "true")
    @Mapping(target = "credentials", expression = "java(Collections.singletonList(representation))")
    UserRepresentation mapToUserRepresentation(UserRequest request, CredentialRepresentation representation);
}
