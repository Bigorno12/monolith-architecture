package mu.server.persistence.repository;

import mu.server.persistence.entity.User;
import mu.server.persistence.repository.blaze.impl.UserCustomRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends GenericRepository<User>, UserCustomRepository {

    Optional<User> findUserByUsername(String username);

    Page<User> findByFirstnameContainsIgnoreCase(Pageable pageable, String firstname);
}
