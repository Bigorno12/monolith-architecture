package mu.server.service.mapper.user;

import mu.server.persistence.projections.UserProjectionDto;
import mu.server.service.dto.UserDto;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserDtoProjectionMapper {

    UserDto mapToUserDto(UserProjectionDto userProjectionDto);

    @InheritInverseConfiguration
    UserProjectionDto mapToUserProjectionDto(UserDto userDto);

}
