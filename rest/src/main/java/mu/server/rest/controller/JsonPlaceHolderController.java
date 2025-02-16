package mu.server.rest.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import mu.server.persistence.entity.User;
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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

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
    public ResponseEntity<Void> retrieveUsers() {
        List<User> users = restClient.get()
                .uri("/users")
                .exchange((request, response) -> {
                    switch (response.getStatusCode()) {
                        case HttpStatus.OK -> {
                            return objectMapper.readValue(response.getBody(), new TypeReference<>() {
                            });
                        }
                        case HttpStatus.NO_CONTENT, HttpStatus.NOT_FOUND, HttpStatus.INTERNAL_SERVER_ERROR ->
                                throw new JsonPlaceHolderException("Failed to get users from jsonplaceholder.typicode.com");
                        default -> throw new IllegalStateException(ERROR_MESSAGE + response.getStatusCode());
                    }
                });

        jsonPlaceHolderService.saveAllUsers(users);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Override
    public ResponseEntity<Void> retrieveTodos() {
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
    public ResponseEntity<Void> retrievePosts() {

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<PostJsonPlaceHolder> postJsonPlaceHolders = IntStream.rangeClosed(1, 100)
                    .mapToObj(index -> executor.submit(() -> restClient.get()
                            .uri("/posts/" + index)
                            .retrieve()
                            .body(PostJsonPlaceHolder.class)))
                    .<PostJsonPlaceHolder>mapMulti((futurePost, consumer) -> {
                        try {
                            consumer.accept(futurePost.get());
                        } catch (InterruptedException | ExecutionException e) {
                            throw new JsonPlaceHolderException("Failed to get posts from jsonplaceholder.typicode.com");
                        }
                    })
                    .toList();

            jsonPlaceHolderService.saveAllPosts(postJsonPlaceHolders);
            return ResponseEntity.status(HttpStatus.CREATED).build();
        }

    }

    @Override
    public ResponseEntity<Void> retrieveComments() {

        List<CompletableFuture<CommentJsonPlaceHolder>> completableFutureComments = IntStream.rangeClosed(1, 500)
                .mapToObj(index -> CompletableFuture.supplyAsync(() -> restClient.get()
                        .uri("/comments/" + index)
                        .retrieve()
                        .body(CommentJsonPlaceHolder.class)))
                .toList();

        List<CommentJsonPlaceHolder> comments = completableFutureComments.stream()
                .map(CompletableFuture::join)
                .toList();

        jsonPlaceHolderService.saveAllComments(comments);

        return ResponseEntity.status(HttpStatus.CREATED).build();

    }
}
