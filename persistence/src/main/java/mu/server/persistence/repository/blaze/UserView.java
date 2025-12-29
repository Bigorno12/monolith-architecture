package mu.server.persistence.repository.blaze;

import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.IdMapping;
import mu.server.persistence.entity.User;

@EntityView(User.class)
public interface UserView {

    @IdMapping
    Long getId();

    String getFirstname();

    String getLastname();

    String getUsername();
}
