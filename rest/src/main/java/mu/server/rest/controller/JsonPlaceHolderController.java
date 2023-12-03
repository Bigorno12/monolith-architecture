package mu.server.rest.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import mu.server.persistence.entity.User;
import mu.server.service.dto.CommentDto;
import mu.server.service.dto.PostDto;
import mu.server.service.dto.TodoDto;
import mu.server.service.dto.UserDto;
import mu.server.service.exception.JsonPlaceHolderException;
import mu.server.service.jsonplaceholder.CommentJsonPlaceHolder;
import mu.server.service.jsonplaceholder.PostJsonPlaceHolder;
import mu.server.service.jsonplaceholder.TodoJsonPlaceHolder;
import mu.server.service.service.JsonPlaceHolderService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class JsonPlaceHolderController implements JsonplaceholderApi {

    private static final String ERROR_MESSAGE = "Unexpected value: ";

    private final JsonPlaceHolderService jsonPlaceHolderService;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;

    public JsonPlaceHolderController(JsonPlaceHolderService jsonPlaceHolderService,
                                     ObjectMapper objectMapper,
                                     @Qualifier("restClient") RestClient restClient) {
        this.jsonPlaceHolderService = jsonPlaceHolderService;
        this.objectMapper = objectMapper;
        this.restClient = restClient;
    }

    @Override
    public ResponseEntity<UserDto> retrieveUsers() {
        List<User> users = restClient.get()
                .uri("/users")
                .exchange((request, response) -> {
                    switch (response.getStatusCode()) {
                        case HttpStatus.OK -> {
                            return objectMapper.readValue(response.getBody(), new TypeReference<>() {
                            });
                        }
                        case HttpStatus.NO_CONTENT, HttpStatus.NOT_FOUND ->
                                throw new JsonPlaceHolderException("Failed to get users from jsonplaceholder.typicode.com");
                        default -> throw new IllegalStateException(ERROR_MESSAGE + response.getStatusCode());
                    }
                });

        jsonPlaceHolderService.saveAllUsers(users);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Override
    public ResponseEntity<TodoDto> retrieveTodos() {
        List<TodoJsonPlaceHolder> todoJsonPlaceHolders = restClient.get()
                .uri("/todos")
                .exchange((request, response) -> {
                    switch (response.getStatusCode()) {
                        case HttpStatus.OK -> {
                            return objectMapper.readValue(response.getBody(), new TypeReference<>() {
                            });
                        }
                        case HttpStatus.NO_CONTENT, HttpStatus.NOT_FOUND ->
                                throw new JsonPlaceHolderException("Failed to get todos from jsonplaceholder.typicode.com");
                        default -> throw new IllegalStateException(ERROR_MESSAGE + response.getStatusCode());
                    }
                });

        jsonPlaceHolderService.saveAllTodos(todoJsonPlaceHolders);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Override
    public ResponseEntity<PostDto> retrievePosts() {

        List<PostJsonPlaceHolder> postJsonPlaceHolders = restClient.get()
                .uri("/posts")
                .exchange((request, response) -> {
                    switch (response.getStatusCode()) {
                        case HttpStatus.OK -> {
                            return objectMapper.readValue(response.getBody(), new TypeReference<>() {
                            });
                        }
                        case HttpStatus.NO_CONTENT, HttpStatus.NOT_FOUND ->
                                throw new JsonPlaceHolderException("Failed to get posts from jsonplaceholder.typicode.com");
                        default -> throw new IllegalStateException(ERROR_MESSAGE + response.getStatusCode());
                    }
                });

        jsonPlaceHolderService.saveAllPosts(postJsonPlaceHolders);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Override
    public ResponseEntity<CommentDto> retrieveComments() {
        List<CommentJsonPlaceHolder> commentJsonPlaceHolders = restClient.get()
                .uri("/comments")
                .exchange((request, response) -> {
                    switch (response.getStatusCode()) {
                        case HttpStatus.OK -> {
                            return objectMapper.readValue(response.getBody(), new TypeReference<>() {
                            });
                        }
                        case HttpStatus.NO_CONTENT, HttpStatus.NOT_FOUND ->
                                throw new JsonPlaceHolderException("Failed to get comments from jsonplaceholder.typicode.com");
                        default -> throw new IllegalStateException(ERROR_MESSAGE + response.getStatusCode());
                    }
                });

        jsonPlaceHolderService.saveAllComments(commentJsonPlaceHolders);

        return ResponseEntity.status(HttpStatus.CREATED).build();

    }


}
