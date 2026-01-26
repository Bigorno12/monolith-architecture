package mu.server.service.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mu.server.persistence.entity.User;
import mu.server.persistence.repository.UserRepository;
import mu.server.service.dto.user.UpdateUserRequest;
import mu.server.service.dto.user.UserResponse;
import mu.server.service.exception.NoFoundException;
import mu.server.service.exception.UsernameExistException;
import mu.server.service.mapper.UserMapper;
import mu.server.service.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    @Transactional(readOnly = true)
    public UserResponse findUserById(Long id) {
        return userRepository.findById(id)
                .map(userMapper::mapToUserResponse)
                .orElseThrow(() -> new NoFoundException("User does not exist!"));
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public UpdateUserRequest updateUser(UpdateUserRequest updateUserRequest, String username) {
        User userName = Optional.ofNullable(userRepository.findByUsername(username))
                .orElseThrow(() -> new NoFoundException("Username not Found!!!"));

        if (!userName.getUsername().equals(updateUserRequest.username())) {
            userRepository.findUserByUsername(updateUserRequest.username())
                    .ifPresent(_ -> {
                                throw new UsernameExistException("Username already exists!!");
                            }
                    );
        }
        userRepository.save(userMapper.updateUserFromDto(updateUserRequest, userName));
        return userMapper.mapToUpdateUser(userName);
    }
}
