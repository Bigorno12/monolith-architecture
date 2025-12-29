package mu.server.service.service.impl;

import lombok.RequiredArgsConstructor;
import mu.server.persistence.entity.User;
import mu.server.persistence.repository.UserRepository;
import mu.server.service.service.AuthUserDetailsService;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthUserDetailsServiceImpl implements AuthUserDetailsService {

    private final UserRepository userRepository;

    @Override
    public @NonNull UserDetails loadUserByUsername(@NonNull String username) throws UsernameNotFoundException {
        return userRepository.findUserByUsername(username)
                .map(user -> new User(user.getUsername(), user.getEmail(), user.getPassword()))
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }
}
