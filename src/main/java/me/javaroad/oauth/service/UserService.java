package me.javaroad.oauth.service;

import java.util.Objects;
import me.javaroad.common.exception.DataConflictException;
import me.javaroad.common.exception.DataNotFoundException;
import me.javaroad.oauth.dto.request.SearchUserRequest;
import me.javaroad.oauth.dto.request.UserRequest;
import me.javaroad.oauth.dto.response.UserResponse;
import me.javaroad.oauth.entity.User;
import me.javaroad.oauth.mapper.UserMapper;
import me.javaroad.oauth.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author heyx
 */
@Service
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository,
        UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserResponse createOrUpdate(UserRequest userRequest) {
        if(Objects.isNull(userRequest.getId())) {
            return create(userRequest);
        }
        return update(userRequest);
    }

    private UserResponse update(UserRequest userRequest) {
        User user = getEntity(userRequest.getId());
        if (Objects.isNull(user)) {
            throw new DataNotFoundException("User[userId=%s] not found", userRequest.getId());
        }
        user.setPassword(passwordEncoder.encode(userRequest.getPassword()));

        user = userRepository.save(user);
        return userMapper.mapEntityToResponse(user);
    }

    private UserResponse create(UserRequest userRequest) {
        User user = getEntity(userRequest.getUsername());
        if (Objects.nonNull(user)) {
            throw new DataConflictException("User[username=%s] already exists", userRequest.getUsername());
        }
        user = userMapper.mapRequestToEntity(userRequest);
        user.setPassword(passwordEncoder.encode(userRequest.getPassword()));

        user = userRepository.save(user);
        return userMapper.mapEntityToResponse(user);
    }

    public Page<UserResponse> getAll(SearchUserRequest searchUserRequest, Pageable pageable) {
        Page<User> userPage = userRepository.findAll(pageable);
        return userPage.map(userMapper::mapEntityToResponse);
    }

    User getEntity(Long id) {
        return userRepository.findOne(id);
    }

    public User getEntity(String username) {
        return userRepository.findByUsername(username);
    }

    public UserResponse getResponse(Long userId) {
        User user = getEntity(userId);
        return userMapper.mapEntityToResponse(user);
    }

    @Transactional
    public void delete(Long userId) {
        User user = getEntity(userId);
        if(Objects.isNull(user)) {
            throw new DataNotFoundException("user[id=%s] not found", userId);
        }
        userRepository.delete(user);
    }

    public UserResponse getResponse(String username) {
        User user = getEntity(username);
        return userMapper.mapEntityToResponse(user);
    }
}
