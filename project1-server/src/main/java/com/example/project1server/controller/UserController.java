package com.example.project1server.controller;
import com.example.project1server.dto.LoginRequest;
import com.example.project1server.dto.RegisterRequest;
import com.example.project1server.dto.UpdateUserRequest;
import com.example.project1server.dto.UserResponse;
import com.example.project1server.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import com.example.project1server.dto.LoginResponse;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public UserResponse register(@Valid @RequestBody RegisterRequest request) {
        return userService.register(request);
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return userService.login(request);
    }

    @GetMapping("/me")
    public UserResponse findCurrentUser(
            @AuthenticationPrincipal Long userId) {
        return userService.findById(userId);
    }

    @PutMapping("/me")
    public UserResponse updateCurrentUser(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody UpdateUserRequest request) {
        return userService.update(userId, request);
    }

    @DeleteMapping("/me")
    public Map<String, String> deleteCurrentUser(
            @AuthenticationPrincipal Long userId) {
        userService.delete(userId);
        return Map.of("message", "删除成功");
    }
}
