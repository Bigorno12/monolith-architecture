package mu.server.persistence.repository.blaze.impl;

import com.blazebit.persistence.CriteriaBuilderFactory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import mu.server.persistence.entity.User;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UserCustomRepositoryImpl implements UserCustomRepository {

    private final CriteriaBuilderFactory cbf;
    private final EntityManager entityManager;

    @Override
    public User findByUsername(String username) {
        return cbf.create(entityManager, User.class)
                .from(User.class, "u")
                .where("u.username").eqExpression(username)
                .getSingleResultOrNull();
    }
}
