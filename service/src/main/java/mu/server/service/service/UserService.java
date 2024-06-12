package mu.server.service.service;

import mu.server.persistence.projections.NamesOnly;
import mu.server.service.dto.UserDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UserService {

    Page<UserDto> findAllUserByName(Pageable pageable, String name);

    List<UserDto> findAllUserDtoByFirstName(String firstName);

    NamesOnly findNameOnlyByUserId(Long userId);
}
