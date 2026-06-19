package com.example.project1server.dto;

import java.math.BigDecimal;

public record RecordSummaryResponse(
        String month,
        BigDecimal totalIncome,
        BigDecimal totalExpense,
        BigDecimal balance
) {}