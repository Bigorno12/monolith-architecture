package mu.server.service.service;

import io.jsonwebtoken.io.IOException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mu.server.service.dto.AuthenticationRequest;
import mu.server.service.dto.AuthenticationResponse;
import mu.server.service.dto.UserRequest;

public interface AuthenticationService {
    AuthenticationResponse register(UserRequest request);

    void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException;

    AuthenticationResponse authenticate(AuthenticationRequest request);
}
