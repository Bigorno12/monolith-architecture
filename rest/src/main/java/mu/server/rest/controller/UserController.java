package mu.server.rest.controller;

import mu.server.service.dto.UserDto;
import mu.server.service.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/user")
public class UserController implements UsersApi {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/find-all-user-by-name")
    public Page<UserDto> findAllUserByName(@RequestParam(name = "pageNum", defaultValue = "0") Integer pageNum, @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize, @RequestParam(name = "name") String name) {
        return userService.findAllUserByName(PageRequest.of(pageNum, pageSize, Sort.by(Sort.Direction.ASC, "name")), name);
    }
}
