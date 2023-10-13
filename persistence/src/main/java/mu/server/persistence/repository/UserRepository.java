package mu.server.persistence.repository;

import mu.server.persistence.entity.User;
import mu.server.persistence.repository.custom.GenericRepository;
import mu.server.persistence.repository.custom.UserCustomRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends GenericRepository<User>, UserCustomRepository {

    Page<User> findByNameContainsIgnoreCase(Pageable pageable, String name);
}
