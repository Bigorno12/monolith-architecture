package mu.server.persistence.repository.blaze;

import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.IdMapping;
import mu.server.persistence.entity.Todo;

@EntityView(Todo.class)
public interface TodoView {

    @IdMapping
    Long getId();

    String getTitle();

    Boolean getCompleted();

    UserView getUser();
}
