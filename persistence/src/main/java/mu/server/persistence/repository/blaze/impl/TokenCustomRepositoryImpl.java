package mu.server.persistence.repository.blaze.impl;

import com.blazebit.persistence.CriteriaBuilderFactory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import mu.server.persistence.entity.Token;
import mu.server.persistence.entity.User;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class TokenCustomRepositoryImpl implements TokenCustomRepository {

    private final CriteriaBuilderFactory cbf;
    private final EntityManager entityManager;

    @Override
    @NonNull
    public List<Token> findTokenByUsernameWhereExpiredOrRevokedIsFalse(@NonNull String username) {
        return cbf.create(entityManager, Token.class)
                .from(Token.class, "t")
                .innerJoinOn(User.class, "u")
                    .on("t.user.id").eqExpression("u.id")
                .end()
                .where("u.username").eq(username)
                .whereOr()
                    .where("t.expired").eq(false)
                    .where("t.revoked").eq(false)
                .endOr()
                .getResultList();

//        return cbf.create(entityManager, Token.class, "t")
//                .select("t")
//                .innerJoin("t.user", "u")
//                .where("u.username").eq(username)
//                .whereOr()
//                    .where("t.expired").eq(false)
//                    .where("t.revoked").eq(false)
//                .endOr()
//                .getResultList();
    }
}
