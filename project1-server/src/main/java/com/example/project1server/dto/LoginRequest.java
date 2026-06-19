package com.example.project1server.dto;

import jakarta.validation.constraints.*;

public record LoginRequest(
        @NotBlank String phone,
        @NotBlank String password
) {}
