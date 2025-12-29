package mu.server.service.mapper.component;

import lombok.RequiredArgsConstructor;
import org.mapstruct.Named;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PasswordEncoderQualifier {
    private final PasswordEncoder passwordEncoder;

    @Named("encodePassword")
    public String encodePassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }
}
