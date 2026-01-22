package mu.server.persistence.repository.blaze.impl;

import com.blazebit.persistence.CriteriaBuilderFactory;
import com.blazebit.persistence.PagedList;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import mu.server.persistence.entity.Todo;
import mu.server.persistence.entity.User;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class TodoCustomRepositoryImpl implements TodoCustomRepository {

    private final CriteriaBuilderFactory cbf;
    private final EntityManager entityManager;

    @Override
    public PagedList<Todo> findTodosByUsername(@NonNull String username, int firstResult, int maxResult) {
        return cbf.create(entityManager, Todo.class)
                .from(Todo.class, "t")
                .orderByAsc("t.id")
                .innerJoinOn(User.class, "u")
                    .on("t.user.id").eqExpression("u.id")
                .end()
                .where("u.username").eq(username)
                .page(firstResult, maxResult)
                .getResultList();
    }
}
