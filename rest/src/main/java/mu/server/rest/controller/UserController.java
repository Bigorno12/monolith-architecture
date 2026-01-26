package mu.server.rest.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import mu.server.service.dto.user.UpdateUserRequest;
import mu.server.service.service.LogoutService;
import mu.server.service.service.UserService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/v1/mono/user", version = "1.0")
public class UserController {

    private final UserService userService;
    private final LogoutService logoutService;

    @PutMapping(value = "/update", version = "1.0")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN') and hasAnyAuthority('user:update', 'admin:update')")
    @Cacheable(cacheNames = "userCache", unless = "#result == null", condition = "#updateUserRequest != null", key = "#updateUserRequest.username()")
    public ResponseEntity<UpdateUserRequest> updateUser(@RequestBody UpdateUserRequest updateUserRequest, @RequestParam(name = "username") String username, HttpServletRequest request, HttpServletResponse response) {
        UpdateUserRequest updateUser = userService.updateUser(updateUserRequest, username);
        logoutService.logout(request, response, SecurityContextHolder.getContext().getAuthentication());
        return ResponseEntity.status(HttpStatus.OK).body(updateUser);
    }
}
