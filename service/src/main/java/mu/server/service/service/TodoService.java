package mu.server.service.service;

import mu.server.service.dto.todo.TodoUsernameResponse;
import mu.server.service.dto.todo.TodosResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TodoService {
    void saveByUserId(Long userId);

    Page<TodoUsernameResponse> findAllTodosByUsername(Pageable pageable, String username);

    Page<TodosResponse> findAllTodos(Pageable pageable);
}
