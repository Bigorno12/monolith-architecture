package mu.server.persistence.repository;

import mu.server.persistence.entity.Todo;
import mu.server.persistence.repository.blaze.impl.TodoCustomRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
public interface TodoRepository extends GenericRepository<Todo>, TodoCustomRepository {
    Page<Todo> findTodosByUser_Username(String userUsername, Pageable pageable);
}
