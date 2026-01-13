package mu.server.persistence.repository.blaze;

import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.IdMapping;
import mu.server.persistence.entity.User;
import mu.server.persistence.enumeration.Gender;

@EntityView(User.class)
public interface UserView {

    @IdMapping
    Long getId();

    String getFirstname();

    String getLastname();

    Integer getAge();

    Gender getGender();

    String getEmail();

    String getUsername();

    String getPassword();
}
