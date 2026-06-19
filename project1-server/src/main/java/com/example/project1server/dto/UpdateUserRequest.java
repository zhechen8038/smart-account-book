package com.example.project1server.dto;

import jakarta.validation.constraints.*;

public record UpdateUserRequest(
        @NotBlank String name,
        @NotNull @Min(1) @Max(120) Integer age,
        @NotBlank String occupation,
        @NotBlank String gender
) {}
