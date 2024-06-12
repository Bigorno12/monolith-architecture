package mu.server.persistence.repository.custom.impl;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQuery;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import mu.server.persistence.entity.QUser;
import mu.server.persistence.entity.User;
import mu.server.persistence.projections.UserProjectionDto;
import mu.server.persistence.repository.custom.UserCustomRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class UserCustomRepositoryImpl implements UserCustomRepository {

    @PersistenceContext
    private final EntityManager entityManager;
    private static final QUser user = QUser.user;

    @Override
    public List<UserProjectionDto> findAllUserDtoByFirstName(@NonNull String firstName) {
        return new JPAQuery<User>(entityManager)
                .select(Projections.constructor(UserProjectionDto.class, user.name, user.username, user.website))
                .from(user)
                .where(user.name.containsIgnoreCase(firstName))
                .fetch();
    }
}
