package mu.server.service.service;

import com.blazebit.persistence.PagedList;
import mu.server.persistence.repository.blaze.TodoView;
import mu.server.service.dto.todo.TodoRequest;
import mu.server.service.dto.todo.TodoUsernameResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface TodoService {

    void save(List<TodoRequest> todoRequest, String username);

    void saveByUserId(Long userId);

    Page<TodoUsernameResponse> findAllTodosByUsername(Pageable pageable, String username);

    PagedList<TodoView> findAllTodos(Pageable pageable);
}
