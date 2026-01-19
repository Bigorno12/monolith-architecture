package mu.server.service.service;

import mu.server.service.dto.todo.TodoUsernameResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TodoService {
    void saveByUserId(Long userId);

    Page<TodoUsernameResponse> findAllTodos(Pageable pageable);
}
