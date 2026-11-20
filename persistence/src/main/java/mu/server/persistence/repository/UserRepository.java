package mu.server.persistence.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import mu.server.persistence.entity.User;

@Repository
public interface UserRepository extends GenericRepository<User> {

    Optional<User> findUserByUsername(String username);

    Page<User> findByFirstnameContainsIgnoreCase(Pageable pageable, String firstname);
}
