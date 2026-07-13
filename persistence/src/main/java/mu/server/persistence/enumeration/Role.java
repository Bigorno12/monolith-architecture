package mu.server.persistence.enumeration;

import static mu.server.persistence.enumeration.Permission.ADMIN_CREATE;
import static mu.server.persistence.enumeration.Permission.ADMIN_DELETE;
import static mu.server.persistence.enumeration.Permission.ADMIN_READ;
import static mu.server.persistence.enumeration.Permission.ADMIN_UPDATE;
import static mu.server.persistence.enumeration.Permission.USER_CREATE;
import static mu.server.persistence.enumeration.Permission.USER_DELETE;
import static mu.server.persistence.enumeration.Permission.USER_UPDATE;

import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Role {

    USER(Set.of(USER_CREATE, ADMIN_READ, USER_DELETE, USER_UPDATE)),
    ADMIN(Set.of(ADMIN_READ, ADMIN_CREATE, ADMIN_DELETE, ADMIN_UPDATE));

    private final Set<Permission> permissions;
}
