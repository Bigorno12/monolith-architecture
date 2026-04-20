package mu.server.service.service

import com.blazebit.persistence.PagedList
import mu.server.persistence.repository.blaze.TodoView
import mu.server.service.dto.todo.TodoRequest
import mu.server.service.dto.todo.TodoUsernameResponse
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface TodoService {

    fun save(todoRequest: List<TodoRequest>?, username: String?)

    fun saveByUserId(userId: Long?)

    fun findAllTodosByUsername(pageable: Pageable?, username: String?): Page<TodoUsernameResponse>?

    fun findAllTodos(pageable: Pageable): PagedList<TodoView>?

}