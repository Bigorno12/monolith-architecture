package mu.server.rest.controller

import com.blazebit.persistence.PagedList
import mu.server.persistence.repository.blaze.TodoView
import mu.server.service.dto.todo.TodoRequest
import mu.server.service.dto.todo.TodoUsernameResponse
import mu.server.service.service.TodoService
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
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
class TodoController(
    private val todoService: TodoService,
) {
    @PostMapping(value = ["/{username}"], version = "1.0")
    @PreAuthorize(value = "hasAuthority('user:create') AND #username == authentication.name")
    fun save(
        @PathVariable username: String?,
    ): ResponseEntity<Void> {
        todoService.saveByUserId(username)
        return ResponseEntity.ok().build()
    }

    @PostMapping(value = ["/save/{username}"], version = "1.0")
    @PreAuthorize(value = "hasAnyAuthority('user:create') AND #username == authentication.name")
    fun save(
        @RequestBody todoRequests: MutableList<TodoRequest>,
        @PathVariable username: String?,
    ): ResponseEntity<Void> {
        todoService.save(todoRequests, username)
        return ResponseEntity.ok().build()
    }

    @GetMapping(value = ["/all-todos/{username}"], version = "1.0")
    @PreAuthorize(value = "hasAuthority('user:read') AND #username == authentication.name")
    fun findAllTodosByUsername(
        @RequestParam(name = "pageNum", defaultValue = "0") pageNum: Int,
        @RequestParam(name = "pageSize", defaultValue = "10") pageSize: Int,
        @PathVariable username: String?,
    ): ResponseEntity<Page<TodoUsernameResponse>> =
        ResponseEntity
            .ok()
            .body(
                todoService.findAllTodosByUsername(
                    PageRequest.of(pageNum, pageSize, Sort.by("id").ascending()),
                    username,
                ),
            )

    @PreAuthorize(value = "hasAuthority('user:read')")
    fun findAllTodos(
        @RequestParam(name = "pageNum", defaultValue = "0") pageNum: Int,
        @RequestParam(name = "pageSize", defaultValue = "10") pageSize: Int,
    ): ResponseEntity<PagedList<TodoView>> =
        ResponseEntity
            .ok()
            .body(todoService.findAllTodos(PageRequest.of(pageNum, pageSize, Sort.by("id").ascending())))
}
