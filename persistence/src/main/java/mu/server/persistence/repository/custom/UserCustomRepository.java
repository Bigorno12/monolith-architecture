package mu.server.persistence.repository.custom;

import mu.server.persistence.entity.User;
import org.springframework.lang.NonNull;

import java.util.List;

public interface UserCustomRepository {

    List<User> findAllByFirstName(@NonNull String firstName);
}
