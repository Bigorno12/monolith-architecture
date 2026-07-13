package mu.server.service.mapper.user;

import java.util.Collections;
import mu.server.service.dto.user.UpdateUserRequest;
import mu.server.service.dto.user.UserRequest;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", imports = {Collections.class}, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface KeycloakMapper {
    @Mapping(target = "enabled", constant = "true")
    @Mapping(target = "credentials", expression = "java(Collections.singletonList(representation))")
    UserRepresentation mapToUserRepresentation(UserRequest request, CredentialRepresentation representation);

    @Mapping(target = "enabled", constant = "true")
    UserRepresentation updateUserKeycloak(UpdateUserRequest updateUserRequest);
}
