package mu.server.persistence.repository;

import mu.server.persistence.entity.User;
import mu.server.persistence.repository.custom.GenericRepository;
import mu.server.persistence.repository.custom.UserCustomRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends GenericRepository<User>, UserCustomRepository {
}
