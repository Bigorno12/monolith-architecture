package mu.server.service.mapper;

import mu.server.persistence.entity.User;
import mu.server.service.dto.UserDto;
import mu.server.service.mapper.user.UserDtoProjectionMapper;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper extends UserDtoProjectionMapper {

    User mapToEntity(UserDto userDto);

    @InheritInverseConfiguration
    UserDto mapToDto(User user);
}
