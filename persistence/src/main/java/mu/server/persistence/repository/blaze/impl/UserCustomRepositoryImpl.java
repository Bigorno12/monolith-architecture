package mu.server.persistence.repository.blaze.impl;

import com.blazebit.persistence.CriteriaBuilderFactory;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.EntityViewSetting;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import mu.server.persistence.entity.User;
import mu.server.persistence.repository.blaze.UserView;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserCustomRepositoryImpl implements UserCustomRepository {

    private final EntityViewManager evm;
    private final CriteriaBuilderFactory cbf;
    private final EntityManager entityManager;


    @Override
    public List<UserView> searchForAllNames(String name) { // Using projections
        return Optional.ofNullable(name)
                .map(String::toLowerCase)
                .map(s -> cbf.create(entityManager, User.class, "u")
                        .select("u.id")
                        .select("u.username")
                        .where("u.username").isNotNull()
                        .where("LOWER(u.username)").like().value("%" + s + "%").noEscape())
                .map(userCriteriaBuilder -> evm.applySetting(EntityViewSetting.create(UserView.class), userCriteriaBuilder)
                        .getResultList())
                .orElse(List.of());
    }

    @Override
    public User findByUsername(String username) {
        return cbf.create(entityManager, User.class)
                .from(User.class, "u")
                .where("u.username").eqExpression(username)
                .getSingleResultOrNull();
    }
}
