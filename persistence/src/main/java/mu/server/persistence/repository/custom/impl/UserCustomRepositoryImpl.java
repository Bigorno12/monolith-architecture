package mu.server.persistence.repository.custom.impl;

import com.querydsl.jpa.impl.JPAQuery;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import mu.server.persistence.entity.QUser;
import mu.server.persistence.entity.User;
import mu.server.persistence.repository.custom.UserCustomRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class UserCustomRepositoryImpl implements UserCustomRepository {

    @PersistenceContext
    private final EntityManager entityManager;

    @Override
    public List<User> findAllByFirstName(@NonNull String firstName) {
        QUser user = QUser.user;
        return new JPAQuery<User>(entityManager)
                .select(user)
                .from(user)
                .fetch();
    }
}
