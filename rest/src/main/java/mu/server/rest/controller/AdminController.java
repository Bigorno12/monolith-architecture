package mu.server.rest.controller;

import lombok.RequiredArgsConstructor;
import mu.server.service.dto.user.UserResponse;
import mu.server.service.service.UserService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/mono/admin")
public class AdminController {

    private final UserService userService;

    @GetMapping(value = "/{id}", version = "1.0")
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('admin:read')")
    @Cacheable(cacheNames = "adminCache", unless = "#result == null", condition = "#id != null", key = "#id")
    public ResponseEntity<UserResponse> findUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.findUserById(id));
    }
}
