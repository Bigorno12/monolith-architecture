package mu.server.service.service.impl;

import com.blazebit.persistence.PagedList;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mu.server.persistence.entity.Todo;
import mu.server.persistence.repository.TodoRepository;
import mu.server.persistence.repository.UserRepository;
import mu.server.persistence.repository.blaze.TodoView;
import mu.server.service.dto.todo.TodoRequest;
import mu.server.service.dto.todo.TodoUsernameResponse;
import mu.server.service.mapper.TodoMapper;
import mu.server.service.service.TodoService;
import mu.server.service.service.http.JsonPlaceHolderService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
    public void save(List<TodoRequest> todoRequest, String username) {
        List<Todo> todos = userRepository.findUserByUsername(username)
                .map(user -> todoMapper.mapTodoRequestAndUserToTodo(todoRequest, user))
                .orElseThrow(() -> new UsernameNotFoundException("Username not found"));

        if (!todos.isEmpty()) {
            todoRepository.saveAll(todos);
        }
    }

    @Override
    @Transactional
    public void saveByUserId(Long userId) {
        userRepository.findById(userId)
                .map(user -> todoMapper.mapTodoRequestAndUserToTodo(jsonPlaceHolderService.todo(userId), user))
                .map(todoRepository::saveAll)
                .orElseThrow(() -> new UsernameNotFoundException("User Does not exists"));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TodoUsernameResponse> findAllTodosByUsername(Pageable pageable, String username) {
        if (userRepository.findUserByUsername(username).isPresent()) {
            return todoRepository.findAll(pageable)
                    .map(todo -> todoMapper.mapToTodoUsernameResponse(todo, username));
        }

        throw new UsernameNotFoundException("User does not exists");
    }

    @Override
    @Transactional(readOnly = true)
    public PagedList<TodoView> findAllTodos(Pageable pageable) {
        return todoRepository.paginationTodos(pageable.getPageNumber(), pageable.getPageSize());
    }
}
