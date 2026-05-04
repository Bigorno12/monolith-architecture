package mu.server.service.service.impl;

import io.jsonwebtoken.io.IOException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import mu.server.persistence.entity.Token;
import mu.server.persistence.entity.User;
import mu.server.persistence.repository.TokenRepository;
import mu.server.persistence.repository.UserRepository;
import mu.server.service.dto.auth.AuthenticationRequest;
import mu.server.service.dto.auth.AuthenticationResponse;
import mu.server.service.dto.auth.TokenResponse;
import mu.server.service.dto.user.UserRequest;
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
                    throw new UsernameExistException("Username already exists " + request.username());
                });

        User user = userRepository.save(userMapper.mapToUser(request));

        TokenResponse tokenResponse = generateToken(user);

        saveToken(user, tokenResponse.getAccessToken());

        return AuthenticationResponse.builder()
                .accessToken(tokenResponse.getAccessToken())
                .refreshToken(tokenResponse.getRefreshToken())
                .build();
    }

    @Override
    public void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
        final String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            final String refreshToken = authorizationHeader.substring(7);
            final String username = jwtService.extractUsername(refreshToken);

            userRepository.findUserByUsername(username).ifPresent(user -> {
                if (jwtService.isTokenValid(refreshToken, user)) {
                    String accessToken = jwtService.generateToken(user);
                    revokeAllUserTokens(username);
                    tokenRepository.save(tokenMapper.mapToEntity(user, accessToken, BEARER));
                }
            });
        }
    }

    @Override
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        User user = userRepository.findUserByUsername(request.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException(request.getUsername()));

        TokenResponse tokenResponse = generateToken(user);

        revokeAllUserTokens(request.getUsername());
        saveToken(user, tokenResponse.getAccessToken());

        return AuthenticationResponse.builder()
                .accessToken(tokenResponse.getAccessToken())
                .refreshToken(tokenResponse.getRefreshToken())
                .build();
    }

    private void revokeAllUserTokens(String username) {
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

    private TokenResponse generateToken(User user) {
        String jwtToken = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        return new TokenResponse(jwtToken, refreshToken);
    }

    private void saveToken(User user, String accessToken) {
        tokenRepository.save(tokenMapper.mapToEntity(user, accessToken, BEARER));
    }
}
