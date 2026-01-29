package mu.server.persistence.repository.blaze.impl;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.CriteriaBuilderFactory;
import com.blazebit.persistence.PagedList;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.EntityViewSetting;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import mu.server.persistence.entity.Todo;
import mu.server.persistence.entity.User;
import mu.server.persistence.repository.blaze.TodoView;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class TodoCustomRepositoryImpl implements TodoCustomRepository {

    private final EntityViewManager evm;
    private final CriteriaBuilderFactory cbf;
    private final EntityManager entityManager;

    @Override
    public PagedList<TodoView> paginationTodos(int firstResult, int maxResult) {
        CriteriaBuilder<Todo> todoCriteriaBuilder = cbf.create(entityManager, Todo.class)
                .from(Todo.class, "t")
                .orderByAsc("t.id")
                .innerJoinOn(User.class, "u")
                .on("t.user.id").eqExpression("u.id")
                .end();

        return evm.applySetting(EntityViewSetting.create(TodoView.class, firstResult, maxResult), todoCriteriaBuilder)
                .getResultList();

    }
}
