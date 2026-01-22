package mu.server.persistence.repository.blaze.impl;

import com.blazebit.persistence.PagedList;
import mu.server.persistence.entity.Todo;
import org.jspecify.annotations.NonNull;

public interface TodoCustomRepository {
    PagedList<Todo> findTodosByUsername(@NonNull String username, int firstResult, int maxResult);
}
