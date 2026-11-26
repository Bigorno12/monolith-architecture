package mu.server.service.mapper.todo;

import java.util.List;

import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import mu.server.persistence.entity.Todo;
import mu.server.persistence.entity.User;
import mu.server.service.dto.todo.TodoRequest;
import mu.server.service.dto.todo.TodoUsernameResponse;
import mu.server.service.dto.todo.TodosResponse;

@Mapper(componentModel = "spring")
public interface TodoMapper {

    TodoUsernameResponse mapToTodoUsernameResponse(Todo todo, String username);

    @Mapping(target = "username", source = "todo.user.username")
    TodosResponse mapToTodosResponse(Todo todo);

    Todo mapDtoToEntity(TodoRequest todoRequest, User user);

    List<Todo> mapDtoToEntity(List<TodoRequest> todoRequest, @Context User user);
}
