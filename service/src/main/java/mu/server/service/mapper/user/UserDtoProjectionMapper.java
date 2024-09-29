package mu.server.service.mapper.user;

import mu.server.persistence.projections.RetrieveUsers;
import mu.server.service.dto.UserDto;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserDtoProjectionMapper {

    UserDto mapToUserDto(RetrieveUsers retrieveUsers);

    @InheritInverseConfiguration
    RetrieveUsers mapToUserProjectionDto(UserDto userDto);

}
