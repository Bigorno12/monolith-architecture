package mu.server.rest.controller;

import mu.server.persistence.projections.NamesOnly;
import mu.server.service.dto.UserDto;
import mu.server.service.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/user")
public class UserController implements UsersApi {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/find-all-user-by-name")
    public Page<UserDto> findAllUserByName(@RequestParam(name = "pageNum", defaultValue = "0") Integer pageNum,
                                           @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                           @RequestParam(name = "name") String name) {
        return userService.findAllUserByName(PageRequest.of(pageNum, pageSize,
                Sort.by(Sort.Direction.ASC, "name")), name);
    }

    @GetMapping("/find-all-user-by-first-name/{firstName}")
    public List<UserDto> findAllUserDtoByFirstName(@PathVariable(name = "firstName") String firstName) {
        return userService.findAllUserDtoByFirstName(firstName);
    }

    @GetMapping("/find-name-only-by-id")
    public NamesOnly findNameOnlyByUserId(@RequestParam Long userId) {
        return userService.findNameOnlyByUserId(userId);
    }

    @GetMapping("")
    public Page<NamesOnly> findNamesOnlyByName(@RequestParam(name = "pageNum", defaultValue = "0") Integer pageNum,
                                               @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                               @RequestParam(name = "name") String name) {
        return userService.findNamesOnlyByName(PageRequest.of(pageNum, pageSize,
                Sort.by(Sort.Direction.ASC, "name")), name);
    }
}
