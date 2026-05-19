package mu.server.service.mapper;

import mu.server.persistence.entity.User;
import mu.server.service.dto.user.UpdateUserRequest;
import mu.server.service.dto.user.UserRequest;
import mu.server.service.dto.user.UserResponse;
import mu.server.service.mapper.component.PasswordEncoderQualifier;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.Collections;

@Mapper(componentModel = "spring", uses = {PasswordEncoderQualifier.class}, imports = {Collections.class})
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(source = "firstName", target = "firstname")
    @Mapping(source = "lastName", target = "lastname")
    @Mapping(source = "password", target = "password", qualifiedByName = "encodePassword")
    User mapToUser(UserRequest userRequest);

    @Deprecated
    @InheritInverseConfiguration
    @Mapping(target = "id", ignore = true)
    User updateUserFromDto(UpdateUserRequest dto, @MappingTarget User user);

    @Deprecated
    UpdateUserRequest mapToUpdateUser(User user);

    @Deprecated
    @Mapping(source = "firstname", target = "firstName")
    @Mapping(source = "lastname", target = "lastName")
    UserResponse mapToUserResponse(User user);

    @Mapping(target = "enabled", constant = "true")
    @Mapping(target = "credentials", expression = "java(Collections.singletonList(representation))")
    UserRepresentation mapToUserRepresentation(UserRequest request, CredentialRepresentation representation);

}
