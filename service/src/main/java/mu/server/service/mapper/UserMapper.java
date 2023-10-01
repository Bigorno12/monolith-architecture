package mu.server.service.mapper;

import mu.server.persistence.entity.User;
import mu.server.service.dto.UserDto;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    User mapToEntity(UserDto userDto);

    @InheritInverseConfiguration
    UserDto mapToDto(User user);
}
