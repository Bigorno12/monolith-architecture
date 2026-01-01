package mu.server.rest.controller;

import lombok.RequiredArgsConstructor;
import mu.server.service.dto.UserResponse;
import mu.server.service.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN')")
@RequestMapping(value = "/api/v1/mono/user", version = "1.0")
public class UserController {

    private final UserService userService;

    @PreAuthorize("hasAnyAuthority('admin:read')")
    @GetMapping(value = "/{id}", version = "1.0")
    ResponseEntity<UserResponse> findUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.findUserById(id));
    }
}
