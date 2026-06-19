package com.example.project1server.dto;

import com.example.project1server.entity.RecordType;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RecordResponse(
        Long id,
        RecordType type,
        String category,
        BigDecimal amount,
        LocalDate recordDate,
        String remark,
        String billImageUrl
) {}