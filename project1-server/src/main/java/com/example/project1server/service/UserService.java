package com.example.project1server.service;

import com.example.project1server.dto.LoginRequest;
import com.example.project1server.dto.RegisterRequest;
import com.example.project1server.dto.UpdateUserRequest;
import com.example.project1server.dto.UserResponse;
import com.example.project1server.entity.User;
import com.example.project1server.exception.BusinessException;
import com.example.project1server.repository.UserRepository;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.project1server.dto.LoginResponse;
import com.example.project1server.security.JwtService;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public UserService(
            UserRepository userRepository,
            BCryptPasswordEncoder passwordEncoder,
            JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    /**
     * 注册用户。
     */
    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByPhone(request.phone())) {
            throw new BusinessException("该手机号已经注册");
        }

        User user = new User();
        user.setPhone(request.phone());

        // 使用 BCrypt 加密密码，不能保存明文密码。
        user.setPassword(passwordEncoder.encode(request.password()));

        user.setName(request.name());
        user.setAge(request.age());
        user.setOccupation(request.occupation());
        user.setGender(request.gender());

        User savedUser = userRepository.save(user);

        return toResponse(savedUser);
    }

    /**
     * 用户登录。
     */
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByPhone(request.phone())
                .orElseThrow(() -> new BusinessException("手机号或密码错误"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BusinessException("手机号或密码错误");
        }

        String token = jwtService.generateToken(user.getId());

        return new LoginResponse(token, toResponse(user));
    }

    /**
     * 根据用户 ID 查询用户信息。
     */
    @Transactional(readOnly = true)
    public UserResponse findById(Long id) {
        User user = findUserById(id);
        return toResponse(user);
    }

    /**
     * 修改用户资料。
     */
    public UserResponse update(Long id, UpdateUserRequest request) {
        User user = findUserById(id);

        user.setName(request.name());
        user.setAge(request.age());
        user.setOccupation(request.occupation());
        user.setGender(request.gender());

        User updatedUser = userRepository.save(user);

        return toResponse(updatedUser);
    }

    /**
     * 根据 ID 查询用户实体。
     */
    private User findUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("用户不存在"));
    }

    /**
     * 将 User 实体转换为响应对象，避免返回密码。
     */
    private UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getPhone(),
                user.getName(),
                user.getAge(),
                user.getOccupation(),
                user.getGender()
        );
    }

    /**
     * 删除用户。
     */
    public void delete(Long id) {
        User user = findUserById(id);
        userRepository.delete(user);
    }
}