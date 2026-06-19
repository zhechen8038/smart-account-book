package com.example.project1server.dto;

public record UserResponse(
        Long id,
        String phone,
        String name,
        Integer age,
        String occupation,
        String gender
) {}
