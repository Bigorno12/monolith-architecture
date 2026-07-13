package mu.server.service.mapper.user;

import java.util.Collections;
import mu.server.persistence.entity.User;
import mu.server.service.dto.user.UpdateUserRequest;
import mu.server.service.dto.user.UserRequest;
import mu.server.service.dto.user.UserResponse;
import mu.server.service.dto.user.ViewUserProfile;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", imports = {Collections.class})
public interface UserMapper extends KeycloakMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(source = "firstName", target = "firstname")
    @Mapping(source = "lastName", target = "lastname")
    User mapToUser(UserRequest userRequest);

    @InheritInverseConfiguration
    @Mapping(target = "id", ignore = true)
    User updateUserFromDto(UpdateUserRequest dto, @MappingTarget User user);

    UpdateUserRequest mapToUpdateUser(User user);

    @Mapping(source = "firstname", target = "firstName")
    @Mapping(source = "lastname", target = "lastName")
    UserResponse mapToUserResponse(User user);

    @Mapping(source = "firstname", target = "firstName")
    @Mapping(source = "lastname", target = "lastName")
    ViewUserProfile mapToUserProfile(User user);

}
