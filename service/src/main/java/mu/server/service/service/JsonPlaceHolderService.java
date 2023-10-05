package mu.server.service.service;

import mu.server.persistence.entity.User;

import java.util.List;

public interface JsonPlaceHolderService {

    void saveAllUsers(List<User> users);

    void saveAllTodos();

    void saveAllPosts();

    void saveAllComments();
}
