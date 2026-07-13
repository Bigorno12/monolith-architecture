package mu.server.persistence.repository;

import java.util.Optional;
import mu.server.persistence.entity.User;
import mu.server.persistence.repository.blaze.impl.UserCustomRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends GenericRepository<User>, UserCustomRepository {

    Optional<User> findUserByUsername(String username);

    Page<User> findByFirstnameContainsIgnoreCase(Pageable pageable, String firstname);
}
