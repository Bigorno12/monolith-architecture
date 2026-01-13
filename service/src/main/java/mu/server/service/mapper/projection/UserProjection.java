package mu.server.service.mapper.projection;

import mu.server.persistence.repository.blaze.UserView;
import mu.server.service.dto.UpdateUserRequest;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserProjection {

    UpdateUserRequest mapToUpdateUserRequest(UserView userView);
}
