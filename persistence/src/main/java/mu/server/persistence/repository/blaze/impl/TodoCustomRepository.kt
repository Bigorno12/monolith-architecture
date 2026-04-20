package mu.server.persistence.repository.blaze.impl

import com.blazebit.persistence.PagedList
import mu.server.persistence.repository.blaze.TodoView

interface TodoCustomRepository {
    fun paginationTodos(firstResult: Int, maxResult: Int): PagedList<TodoView>?
}