package mu.server.persistence.repository.blaze.impl;

import mu.server.persistence.repository.blaze.UserView;

import java.util.List;

public interface UserCustomRepository {
    List<UserView> searchForAllNames(String name);

}
