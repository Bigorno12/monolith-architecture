package mu.server.rest.controller;

import mu.server.persistence.entity.User;
import mu.server.rest.config.RestTemplateConfig;
import mu.server.service.service.JsonPlaceHolderService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/jsonplaceholder")
public class JsonPlaceHolderController {

    private final JsonPlaceHolderService jsonPlaceHolderService;
    private final RestTemplateConfig restTemplateConfig;

    public JsonPlaceHolderController(JsonPlaceHolderService jsonPlaceHolderService, RestTemplateConfig restTemplateConfig) {
        this.jsonPlaceHolderService = jsonPlaceHolderService;
        this.restTemplateConfig = restTemplateConfig;
    }

    @GetMapping("/users")
    public void saveAllUsers() {
        List<User> users = Optional.ofNullable(restTemplateConfig.restTemplateJsonPlaceHolder()
                        .getForEntity("/users", User[].class)
                        .getBody())
                .map(Arrays::asList)
                .orElseThrow();
        jsonPlaceHolderService.saveAllUsers(users);
    }
}
