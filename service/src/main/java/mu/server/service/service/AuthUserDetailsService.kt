package mu.server.service.service;

import org.jspecify.annotations.NonNull;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public interface AuthUserDetailsService extends UserDetailsService {
    @Override
    @NonNull UserDetails loadUserByUsername(@NonNull String username) throws UsernameNotFoundException;
}
