package mu.server.persistence.repository.blaze.impl;

import mu.server.persistence.entity.Token;

import java.util.List;

public interface TokenCustomRepository {
    List<Token> findTokenByUsernameWhereExpiredOrRevokedIsFalse(String username);
}
