package mu.server.service.mapper.user;

import mu.server.persistence.repository.blaze.UserView;
import mu.server.service.dto.UserDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserDtoProjectionMapper {

    UserDto mapToUserView(UserView userView);

}
