package com.example.project1server.dto;


import com.example.project1server.entity.RecordType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SaveRecordRequest(
        @NotNull RecordType type,
        @NotBlank String category,
        @NotNull @DecimalMin("0.01") BigDecimal amount,
        @NotNull LocalDate recordDate,
        @Size(max = 500) String remark
) {}
