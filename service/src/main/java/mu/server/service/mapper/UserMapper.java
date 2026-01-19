package mu.server.service.mapper;

import mu.server.persistence.entity.User;
import mu.server.service.dto.user.UpdateUserRequest;
import mu.server.service.dto.user.UserRequest;
import mu.server.service.dto.user.UserResponse;
import mu.server.service.mapper.component.PasswordEncoderQualifier;
import mu.server.service.mapper.projection.UserProjection;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", uses = {PasswordEncoderQualifier.class})
public interface UserMapper extends UserProjection {

    @Mapping(target = "id", ignore = true)
    @Mapping(source = "firstName", target = "firstname")
    @Mapping(source = "lastName", target = "lastname")
    @Mapping(source = "password", target = "password", qualifiedByName = "encodePassword")
    User mapToUser(UserRequest userRequest);

    @InheritInverseConfiguration
    @Mapping(target = "id", ignore = true)
    User updateUserFromDto(UpdateUserRequest dto, @MappingTarget User user);

    @Mapping(source = "firstname", target = "firstName")
    @Mapping(source = "lastname", target = "lastName")
    UserResponse mapToUserResponse(User user);
}
