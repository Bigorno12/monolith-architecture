package mu.server.service.service;

import mu.server.persistence.entity.User;
import mu.server.service.jsonplaceholder.CommentJsonPlaceHolder;
import mu.server.service.jsonplaceholder.PostJsonPlaceHolder;
import mu.server.service.jsonplaceholder.TodoJsonPlaceHolder;

import java.util.List;

public interface JsonPlaceHolderService {

    void saveAllUsers(List<User> users);

    void saveAllTodos(List<TodoJsonPlaceHolder> todoJsonPlaceHolders);

    void saveAllPosts(List<PostJsonPlaceHolder> postJsonPlaceHolders);

    void saveAllComments(List<CommentJsonPlaceHolder> commentJsonPlaceHolders);
}
