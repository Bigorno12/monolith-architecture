package mu.server.service.service.impl;

import lombok.extern.slf4j.Slf4j;
import mu.server.persistence.projections.NamesOnly;
import mu.server.persistence.repository.UserRepository;
import mu.server.service.dto.UserDto;
import mu.server.service.mapper.UserMapper;
import mu.server.service.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserDto> findAllUserByName(Pageable pageable, String name) {
        return userRepository.findByNameContainsIgnoreCase(pageable, name)
                .map(userMapper::mapToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> findAllUserDtoByFirstName(String firstName) {
        return userRepository.findAllUserDtoByFirstName(firstName)
                .stream()
                .map(userMapper::mapToUserDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public NamesOnly findNameOnlyByUserId(Long userId) {
        return userRepository.findNamesOnlyById(userId);
    }

    @Override
    public Page<NamesOnly> findNamesOnlyByName(Pageable pageable, String name) {
        return userRepository.findNamesOnlyByNameContainsIgnoreCase(pageable, name);
    }
}
