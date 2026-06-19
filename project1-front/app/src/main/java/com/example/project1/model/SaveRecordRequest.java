package com.example.project1.model;

import java.math.BigDecimal;

public class SaveRecordRequest {
    public String type;
    public String category;
    public BigDecimal amount;
    public String recordDate;
    public String remark;

    public SaveRecordRequest(
            String type,
            String category,
            BigDecimal amount,
            String recordDate,
            String remark
    ) {
        this.type = type;
        this.category = category;
        this.amount = amount;
        this.recordDate = recordDate;
        this.remark = remark;
    }
}