package mu.server.persistence.repository;

import mu.server.persistence.entity.Todo;
import mu.server.persistence.repository.custom.GenericRepository;
import mu.server.persistence.repository.custom.TodoCustomRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TodoRepository extends GenericRepository<Todo>, TodoCustomRepository {
}
