package mu.server.service.service.impl;

import com.blazebit.persistence.PagedList;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mu.server.persistence.entity.Todo;
import mu.server.persistence.entity.User;
import mu.server.persistence.repository.TodoRepository;
import mu.server.persistence.repository.UserRepository;
import mu.server.persistence.repository.blaze.TodoView;
import mu.server.service.dto.todo.TodoRequest;
import mu.server.service.dto.todo.TodoUsernameResponse;
import mu.server.service.exception.InvalidCallException;
import mu.server.service.exception.JsonPlaceHolderException;
import mu.server.service.exception.NotFoundException;
import mu.server.service.mapper.todo.TodoMapper;
import mu.server.service.service.TodoService;
import mu.server.service.service.http.JsonPlaceHolderService;
import org.jspecify.annotations.Nullable;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TodoServiceImpl implements TodoService {

    private final TodoRepository todoRepository;
    private final UserRepository userRepository;
    private final TodoMapper todoMapper;
    private final JsonPlaceHolderService jsonPlaceHolderService;


    @Override
    @Transactional
    @Cacheable(
            cacheNames = "todoCache",
            unless = "#result == null",
            key = "#username",
            condition = "#username != null OR todoRequest != null"
    )
    public void save(List<TodoRequest> todoRequest, String username) {
        User user = userRepository.findUserByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Username not found: " + username));

        List<Todo> todos = todoMapper.mapDtoToEntity(todoRequest, user);

        if (!todos.isEmpty()) {
            todoRepository.saveAll(todos);
        }
    }

    @Override
    @Transactional
    @Cacheable(cacheNames = "todoCache", unless = "#result == null", key = "#username", condition = "#username != null")
    public void saveByUserId(@Nullable String username) {
        User user = userRepository.findUserByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User Does not exists: " + username));

        List<Todo> todos = retrieveTodoByUserId(user.getId())
                .stream()
                .filter(Objects::nonNull)
                .map(todoRequest -> todoMapper.mapDtoToEntity(todoRequest, user))
                .toList();

        todoRepository.saveAll(todos);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "todoCache", unless = "#result == null", key = "#username", condition = "#username != null")
    public Page<TodoUsernameResponse> findAllTodosByUsername(Pageable pageable, String username) {
        if (userRepository.findUserByUsername(username).isPresent()) {
            return todoRepository.findAll(pageable)
                    .map(todo -> todoMapper.mapToTodoUsernameResponse(todo, username));
        }

        throw new UsernameNotFoundException("User does not exists");
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "todoCache", unless = "#result == null")
    public PagedList<TodoView> findAllTodos(Pageable pageable) {
        return todoRepository.paginationTodos(pageable.getPageNumber(), pageable.getPageSize());
    }

    private List<TodoRequest> retrieveTodoByUserId(Long userId) {
        try {
            return jsonPlaceHolderService.todo(userId);
        } catch (NotFoundException | InvalidCallException | JsonPlaceHolderException e) {
            log.error("Error message when retrieving todos api: {}", e.getMessage());
            return List.of();
        }
    }
}
