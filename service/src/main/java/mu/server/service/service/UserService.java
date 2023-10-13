package mu.server.service.service;

import mu.server.service.dto.UserDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {

    Page<UserDto> findAllUserByName(Pageable pageable, String name);
}
