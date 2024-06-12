package mu.server.persistence.repository.custom;

import mu.server.persistence.projections.UserProjectionDto;
import org.springframework.lang.NonNull;

import java.util.List;

public interface UserCustomRepository {
    List<UserProjectionDto> findAllUserDtoByFirstName(@NonNull String firstName);
}
