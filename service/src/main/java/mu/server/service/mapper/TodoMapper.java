package mu.server.service.mapper;

import mu.server.persistence.entity.Todo;
import mu.server.persistence.entity.User;
import mu.server.service.dto.todo.TodoRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.ArrayList;
import java.util.List;

@Mapper(componentModel = "spring")
public interface TodoMapper {

    @Mapping(target = "id", ignore = true)
    Todo mapToEntity(TodoRequest todoRequest);

    default List<Todo> mapTodoRequestAndUserToTodo(List<TodoRequest> request, User user) {

        List<Todo> todos = new ArrayList<>();

        for (TodoRequest todoRequest : request) {
            Todo todo = new Todo();
            if (todoRequest != null) {
                todo.setTitle(todoRequest.title());
                todo.setCompleted(todoRequest.completed());
            }

            if (user != null) {
                todo.setUser(user);
            }
            todos.add(todo);
        }

        return todos;
    }
}
