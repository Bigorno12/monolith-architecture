package mu.server.persistence.repository.blaze.impl;

import mu.server.persistence.entity.User;

public interface UserCustomRepository {
    User findByUsername(String username);
}
