package mu.server.rest.controller;

import mu.server.persistence.entity.User;
import mu.server.rest.config.RestTemplateConfig;
import mu.server.service.dto.CommentDto;
import mu.server.service.dto.PostDto;
import mu.server.service.dto.TodoDto;
import mu.server.service.dto.UserDto;
import mu.server.service.exception.JsonPlaceHolderException;
import mu.server.service.jsonplaceholder.CommentJsonPlaceHolder;
import mu.server.service.jsonplaceholder.PostJsonPlaceHolder;
import mu.server.service.jsonplaceholder.TodoJsonPlaceHolder;
import mu.server.service.service.JsonPlaceHolderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1")
public class JsonPlaceHolderController implements JsonplaceholderApi {

    private final JsonPlaceHolderService jsonPlaceHolderService;
    private final RestTemplateConfig restTemplateConfig;

    public JsonPlaceHolderController(JsonPlaceHolderService jsonPlaceHolderService, RestTemplateConfig restTemplateConfig) {
        this.jsonPlaceHolderService = jsonPlaceHolderService;
        this.restTemplateConfig = restTemplateConfig;
    }

    @Override
    public ResponseEntity<UserDto> retrieveUsers() {
        List<User> users = Optional.ofNullable(restTemplateConfig.restTemplateJsonPlaceHolder()
                        .getForEntity("/users", User[].class)
                        .getBody())
                .map(Arrays::asList)
                .orElseThrow(() -> new JsonPlaceHolderException("Failed to get users from jsonplaceholder.typicode.com"));

        jsonPlaceHolderService.saveAllUsers(users);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Override
    public ResponseEntity<TodoDto> retrieveTodos() {
        List<TodoJsonPlaceHolder> todoJsonPlaceHolders = Optional.ofNullable(restTemplateConfig.restTemplateJsonPlaceHolder()
                        .getForEntity("/todos", TodoJsonPlaceHolder[].class)
                        .getBody())
                .map(Arrays::asList)
                .orElseThrow(() -> new JsonPlaceHolderException("Failed to get todos from jsonplaceholder.typicode.com"));
        jsonPlaceHolderService.saveAllTodos(todoJsonPlaceHolders);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Override
    public ResponseEntity<PostDto> retrievePosts() {

        List<PostJsonPlaceHolder> postJsonPlaceHolders = Optional.ofNullable(restTemplateConfig.restTemplateJsonPlaceHolder()
                        .getForEntity("/posts", PostJsonPlaceHolder[].class)
                        .getBody())
                .map(Arrays::asList)
                .orElseThrow(() -> new JsonPlaceHolderException("Failed to get posts from jsonplaceholder.typicode.com"));

        jsonPlaceHolderService.saveAllPosts(postJsonPlaceHolders);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Override
    public ResponseEntity<CommentDto> retrieveComments() {
        List<CommentJsonPlaceHolder> commentJsonPlaceHolders = Optional.ofNullable(restTemplateConfig.restTemplateJsonPlaceHolder()
                        .getForEntity("/comments", CommentJsonPlaceHolder[].class)
                        .getBody())
                .map(Arrays::asList)
                .orElseThrow(() -> new JsonPlaceHolderException("Failed to get comments from jsonplaceholder.typicode.com"));

        jsonPlaceHolderService.saveAllComments(commentJsonPlaceHolders);

        return ResponseEntity.status(HttpStatus.CREATED).build();

    }


}
