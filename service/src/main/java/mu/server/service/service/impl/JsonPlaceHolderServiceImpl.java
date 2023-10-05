package mu.server.service.service.impl;

import lombok.extern.slf4j.Slf4j;
import mu.server.persistence.entity.User;
import mu.server.persistence.repository.TodoRepository;
import mu.server.persistence.repository.UserRepository;
import mu.server.service.service.JsonPlaceHolderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@Transactional
public class JsonPlaceHolderServiceImpl implements JsonPlaceHolderService {

    private final UserRepository userRepository;
    private final TodoRepository todoRepository;

    @Autowired
    public JsonPlaceHolderServiceImpl(UserRepository userRepository, TodoRepository todoRepository) {
        this.userRepository = userRepository;
        this.todoRepository = todoRepository;
    }

    @Override
    public void saveAllUsers(List<User> users) {

    }

    @Override
    public void saveAllTodos() {

    }

    @Override
    public void saveAllPosts() {

    }

    @Override
    public void saveAllComments() {

    }
}
