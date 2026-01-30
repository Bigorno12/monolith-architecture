package mu.server.persistence.repository.blaze.impl;

import com.blazebit.persistence.PagedList;
import mu.server.persistence.repository.blaze.TodoView;

public interface TodoCustomRepository {
    PagedList<TodoView> paginationTodos(int firstResult, int maxResult);
}
