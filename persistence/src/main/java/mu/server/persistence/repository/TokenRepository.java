package mu.server.persistence.repository;

import mu.server.persistence.entity.Token;
import mu.server.persistence.repository.blaze.impl.TokenCustomRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TokenRepository extends GenericRepository<Token>, TokenCustomRepository {
    Optional<Token> findByToken(String token);
}
