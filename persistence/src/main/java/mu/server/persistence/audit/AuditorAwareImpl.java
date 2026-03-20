package mu.server.persistence.audit;

import org.jspecify.annotations.NonNull;import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

public class AuditorAwareImpl implements AuditorAware<String> {
    @Override
    @NonNull
    public Optional<String> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }

        if (authentication.getPrincipal() instanceof UserDetails userDetails && authentication.getPrincipal() != null) {
            return Optional.of(userDetails.getUsername());
        } else {
            return Optional.ofNullable(authentication.getPrincipal())
                    .map(Object::toString);
        }
    }
}
