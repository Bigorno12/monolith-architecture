package mu.server.service.service;

import io.jsonwebtoken.io.IOException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mu.server.service.dto.auth.AuthenticationRequest;
import mu.server.service.dto.auth.AuthenticationResponse;
import mu.server.service.dto.user.UserRequest;

public interface AuthenticationService {
    AuthenticationResponse register(UserRequest request);

    void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException;

    AuthenticationResponse authenticate(AuthenticationRequest request);
}
