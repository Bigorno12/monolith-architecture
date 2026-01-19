package mu.server.service.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mu.server.persistence.repository.TodoRepository;
import mu.server.persistence.repository.UserRepository;
import mu.server.service.dto.todo.TodoUsernameResponse;
import mu.server.service.mapper.TodoMapper;
import mu.server.service.service.TodoService;
import mu.server.service.service.http.JsonPlaceHolderService;
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
    public void saveByUserId(Long userId) {
            userRepository.findById(userId)
                    .map(user -> todoMapper.mapTodoRequestAndUserToTodo(jsonPlaceHolderService.todo(userId), user))
                    .map(todoRepository::saveAll)
                    .orElseThrow(() -> new UsernameNotFoundException("User Does not exists"));
    }

    @Override
    public Page<TodoUsernameResponse> findAllTodos(Pageable pageable) {

        return null;
    }
}
