package mu.server.rest.controller;

import com.blazebit.persistence.PagedList;
import lombok.RequiredArgsConstructor;
import mu.server.persistence.repository.blaze.TodoView;
import mu.server.service.dto.todo.TodoRequest;
import mu.server.service.dto.todo.TodoUsernameResponse;
import mu.server.service.service.TodoService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/v1/mono/todo", version = "1.0")
public class TodoController {

    private final TodoService todoService;

    @PostMapping(value = "/{userId}", version = "1.0")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN') and hasAnyAuthority('user:create', 'admin:create')")
    public ResponseEntity<Void> save(@PathVariable Long userId) {
        todoService.saveByUserId(userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping(value = "/save/{username}", version = "1.0")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN') and hasAnyAuthority('user:create', 'admin:create')")
    @Cacheable(cacheNames = "todoCache", unless = "#result == null", key = "#username", condition = "#username != null")
    public ResponseEntity<Void> save(@RequestBody List<TodoRequest> todoRequests, @PathVariable String username) {
        todoService.save(todoRequests, username);
        return ResponseEntity.ok().build();
    }

    @GetMapping(value = "/all-todos/{username}", version = "1.0")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN') and hasAnyAuthority('user:read', 'admin:read')")
    public ResponseEntity<Page<TodoUsernameResponse>> findAllTodosByUsername(@RequestParam(name = "pageNum", defaultValue = "0") Integer pageNum,
                                                                             @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                                                             @PathVariable String username) {
        return ResponseEntity.ok()
                .body(todoService.findAllTodosByUsername(PageRequest.of(pageNum, pageSize), username));
    }

    @GetMapping(value = "/all-todos", version = "1.0")
    @Cacheable(cacheNames = "todoCache", unless = "#result == null")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN') and hasAnyAuthority('user:read', 'admin:read')")
    public ResponseEntity<PagedList<TodoView>> findAllTodos(@RequestParam(name = "pageNum", defaultValue = "0") Integer pageNum,
                                                            @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize) {
        return ResponseEntity.ok()
                .body(todoService.findAllTodos(PageRequest.of(pageNum, pageSize)));
    }
}
