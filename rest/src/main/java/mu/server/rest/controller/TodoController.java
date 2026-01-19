package mu.server.rest.controller;

import lombok.RequiredArgsConstructor;
import mu.server.service.service.TodoService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/v1/mono/todo", version = "1.0")
public class TodoController {

    private final TodoService todoService;

    @PostMapping(value = "/{userId}", version = "1.0")
    @PreAuthorize("hasAnyRole('USER') and hasAnyAuthority('user:create')")
    public ResponseEntity<Void> save(@PathVariable Long userId) {
        todoService.saveByUserId(userId);
        return ResponseEntity.ok().build();
    }
}
