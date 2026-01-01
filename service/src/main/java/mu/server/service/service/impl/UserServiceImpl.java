package mu.server.service.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mu.server.persistence.repository.UserRepository;
import mu.server.service.dto.UserResponse;
import mu.server.service.exception.NoFoundException;
import mu.server.service.mapper.UserMapper;
import mu.server.service.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    @PreAuthorize("hasAuthority('admin:read')")
    @Transactional(readOnly = true)
    public UserResponse findUserById(Long id) {
        return userRepository.findById(id)
                .map(userMapper::mapToUserResponse)
                .orElseThrow(() -> new NoFoundException("User does not exist!"));
    }
}
