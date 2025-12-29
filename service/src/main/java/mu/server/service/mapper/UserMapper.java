package mu.server.service.mapper;

import mu.server.persistence.entity.User;
import mu.server.service.dto.UserDto;
import mu.server.service.dto.UserRequest;
import mu.server.service.mapper.component.PasswordEncoderQualifier;
import mu.server.service.mapper.user.UserDtoProjectionMapper;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {PasswordEncoderQualifier.class})
public interface UserMapper extends UserDtoProjectionMapper {

    User mapToEntity(UserDto userDto);

    @InheritInverseConfiguration
    UserDto mapToDto(User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(source = "firstName", target = "firstname")
    @Mapping(source = "lastName", target = "lastname")
    @Mapping(source = "password", target = "password", qualifiedByName = "encodePassword")
    User mapToUser(UserRequest userRequest);
}
