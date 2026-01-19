package mu.server.service.service.impl;

import io.jsonwebtoken.io.IOException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import mu.server.persistence.entity.Token;
import mu.server.persistence.entity.User;
import mu.server.persistence.repository.TokenRepository;
import mu.server.persistence.repository.UserRepository;
import mu.server.service.dto.AuthenticationRequest;
import mu.server.service.dto.AuthenticationResponse;
import mu.server.service.dto.UserRequest;
import mu.server.service.exception.UsernameExistException;
import mu.server.service.mapper.TokenMapper;
import mu.server.service.mapper.UserMapper;
import mu.server.service.service.AuthenticationService;
import mu.server.service.service.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static mu.server.persistence.enumeration.TokenType.BEARER;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserMapper userMapper;
    private final TokenMapper tokenMapper;

    @Override
    @Transactional
    public AuthenticationResponse register(UserRequest request) {

        userRepository.findUserByUsername(request.username())
                .ifPresent(_ -> {
                    throw new UsernameExistException("Username already exists" + request.username());
                });
        User user = userRepository.save(userMapper.mapToUser(request));

        String jwtToken = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        tokenRepository.save(tokenMapper.mapToEntity(user, jwtToken, BEARER));

        return AuthenticationResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Override
    public void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
        final String authorizationHeader = request.getHeader("Authorization");
        final String refreshToken;
        final String username;

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            refreshToken = authorizationHeader.substring(7);
            username = jwtService.extractUsername(refreshToken);

            userRepository.findUserByUsername(username).ifPresent(u -> {
                if (jwtService.isTokenValid(refreshToken, u)) {
                    String accessToken = jwtService.generateToken(u);
                    revokeAllUserTokens(username);
                    saveTokenUser(u, accessToken);
                }
            });
        }
    }

    @Override
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.username(), request.password()));

        User user = userRepository.findUserByUsername(request.username())
                .orElseThrow(() -> new UsernameNotFoundException(request.username()));

        String jwtToken = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        revokeAllUserTokens(request.username());
        saveTokenUser(user, jwtToken);

        return AuthenticationResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken)
                .build();
    }

    public void revokeAllUserTokens(String username) {
        List<Token> validUsernameToken = tokenRepository.findTokenByUsernameWhereExpiredOrRevokedIsFalse(
                username
        );

        if (!validUsernameToken.isEmpty()) {
            for (Token token : validUsernameToken) {
                token.setRevoked(true);
                token.setExpired(true);
            }

            tokenRepository.saveAll(validUsernameToken);
        }
    }

    public void saveTokenUser(User user, String jwtToken) {
        tokenRepository.save(tokenMapper.mapToEntity(user, jwtToken, BEARER));
    }
}
