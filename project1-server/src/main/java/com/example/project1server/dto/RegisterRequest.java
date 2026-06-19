package com.example.project1server.dto;

import jakarta.validation.constraints.*;

public record RegisterRequest(
        @NotBlank @Pattern(regexp = "^1[3-9]\\d{9}$") String phone,
        @NotBlank @Size(min = 6, max = 50) String password,
        @NotBlank String name,
        @NotNull @Min(1) @Max(120) Integer age,
        @NotBlank String occupation,
        @NotBlank String gender
) {}
