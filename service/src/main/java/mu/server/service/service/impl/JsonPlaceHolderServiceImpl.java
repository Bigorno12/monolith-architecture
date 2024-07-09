package mu.server.service.service.impl;

import lombok.extern.slf4j.Slf4j;
import mu.server.persistence.entity.Comment;
import mu.server.persistence.entity.Post;
import mu.server.persistence.entity.Todo;
import mu.server.persistence.entity.User;
import mu.server.persistence.repository.CommentRepository;
import mu.server.persistence.repository.PostRepository;
import mu.server.persistence.repository.TodoRepository;
import mu.server.persistence.repository.UserRepository;
import mu.server.service.exception.JsonPlaceHolderException;
import mu.server.service.jsonplaceholder.CommentJsonPlaceHolder;
import mu.server.service.jsonplaceholder.PostJsonPlaceHolder;
import mu.server.service.jsonplaceholder.TodoJsonPlaceHolder;
import mu.server.service.service.JsonPlaceHolderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class JsonPlaceHolderServiceImpl implements JsonPlaceHolderService {

    private final UserRepository userRepository;
    private final TodoRepository todoRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    @Autowired
    public JsonPlaceHolderServiceImpl(UserRepository userRepository, TodoRepository todoRepository, PostRepository postRepository, CommentRepository commentRepository) {
        this.userRepository = userRepository;
        this.todoRepository = todoRepository;
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
    }

    @Override
    public void saveAllUsers(List<User> users) {
        userRepository.saveAll(users);
    }

    @Override
    @Transactional
    public void saveAllTodos(List<TodoJsonPlaceHolder> todoJsonPlaceHolders) {
        List<Todo> mapToTodos = todoJsonPlaceHolders.stream()
                .map(this::mapTodosJsonPlaceHolderToEntity)
                .toList();

        todoRepository.saveAll(mapToTodos);
    }

    @Override
    @Transactional
    public void saveAllPosts(List<PostJsonPlaceHolder> postJsonPlaceHolders) {
        List<Post> posts = postJsonPlaceHolders.stream()
                .map(this::mapPostJsonPlaceHolderToEntity)
                .toList();

        postRepository.saveAll(posts);
    }

    @Override
    @Transactional
    public void saveAllComments(List<CommentJsonPlaceHolder> commentJsonPlaceHolders) {
        List<Comment> comments = commentJsonPlaceHolders.stream()
                .map(this::mapCommentJsonPlaceHolderToEntity)
                .toList();

        commentRepository.saveAll(comments);
    }

    private Todo mapTodosJsonPlaceHolderToEntity(TodoJsonPlaceHolder todoJsonPlaceHolder) {
        return Todo.builder()
                .id(todoJsonPlaceHolder.id())
                .user(userRepository.findById(todoJsonPlaceHolder.userId()).orElseThrow(() -> new JsonPlaceHolderException("User not found")))
                .title(todoJsonPlaceHolder.title())
                .completed(todoJsonPlaceHolder.completed())
                .build();
    }

    private Post mapPostJsonPlaceHolderToEntity(PostJsonPlaceHolder postJsonPlaceHolder) {
        return Post.builder()
                .id(postJsonPlaceHolder.id())
                .user(userRepository.findById(postJsonPlaceHolder.userId()).orElseThrow(() -> new JsonPlaceHolderException("User not found")))
                .title(postJsonPlaceHolder.title())
                .body(postJsonPlaceHolder.body())
                .build();
    }

    private Comment mapCommentJsonPlaceHolderToEntity(CommentJsonPlaceHolder commentJsonPlaceHolder) {
        return Comment.builder()
                .id(commentJsonPlaceHolder.id())
                .post(postRepository.findById(commentJsonPlaceHolder.postId()).orElseThrow(() -> new JsonPlaceHolderException("Post not found")))
                .email(commentJsonPlaceHolder.email())
                .body(commentJsonPlaceHolder.body())
                .build();
    }
}
