package com.example.project1server.dto;

public record LoginResponse(
        String token,
        UserResponse user
) {}
