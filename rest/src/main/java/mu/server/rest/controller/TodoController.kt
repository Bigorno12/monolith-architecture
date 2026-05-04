package mu.server.rest.controller

import com.blazebit.persistence.PagedList
import mu.server.persistence.repository.blaze.TodoView
import mu.server.service.dto.todo.TodoRequest
import mu.server.service.dto.todo.TodoUsernameResponse
import mu.server.service.service.TodoService
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(version = "1.0", value = ["/api/v1/mono/todo"], produces = ["application/json"])
class TodoController(private val todoService: TodoService) {

    @PostMapping(value = ["/{username}"], version = "1.0")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN') and hasAnyAuthority('user:create', 'admin:create')")
    fun save(@PathVariable username: String?): ResponseEntity<Void> {
        todoService.saveByUserId(username)
        return ResponseEntity.ok().build()
    }

    @PostMapping(value = ["/save/{username}"], version = "1.0")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN') and hasAnyAuthority('user:create', 'admin:create')")
    @Cacheable(
        cacheNames = ["todoCache"],
        unless = "#result == null",
        key = "#username",
        condition = "#username != null"
    )
    fun save(
        @RequestBody todoRequests: MutableList<TodoRequest>,
        @PathVariable username: String?
    ): ResponseEntity<Void> {
        todoService.save(todoRequests, username)
        return ResponseEntity.ok().build()
    }

    @GetMapping(value = ["/all-todos/{username}"], version = "1.0")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN') and hasAnyAuthority('user:read', 'admin:read')")
    fun findAllTodosByUsername(
        @RequestParam(name = "pageNum", defaultValue = "0") pageNum: Int,
        @RequestParam(name = "pageSize", defaultValue = "10") pageSize: Int,
        @PathVariable username: String?
    ): ResponseEntity<Page<TodoUsernameResponse>> = ResponseEntity.ok()
        .body(todoService.findAllTodosByUsername(PageRequest.of(pageNum, pageSize), username))

    @GetMapping(value = ["/all-todos"], version = "1.0")
    @Cacheable(cacheNames = ["todoCache"], unless = "#result == null")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN') and hasAnyAuthority('user:read', 'admin:read')")
    fun findAllTodos(
        @RequestParam(name = "pageNum", defaultValue = "0") pageNum: Int,
        @RequestParam(name = "pageSize", defaultValue = "10") pageSize: Int
    ): ResponseEntity<PagedList<TodoView>> {
        return ResponseEntity.ok()
            .body(todoService.findAllTodos(PageRequest.of(pageNum, pageSize)))
    }

}