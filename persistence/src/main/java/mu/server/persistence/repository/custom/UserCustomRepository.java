package mu.server.persistence.repository.custom;

import mu.server.persistence.projections.RetrieveUsers;
import org.springframework.lang.NonNull;

import java.util.List;

public interface UserCustomRepository {
    List<RetrieveUsers> findAllUserDtoByFirstName(@NonNull String firstName);
}
