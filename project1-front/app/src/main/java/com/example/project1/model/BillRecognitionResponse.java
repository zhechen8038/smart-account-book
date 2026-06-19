package com.example.project1.model;

import java.math.BigDecimal;

public class BillRecognitionResponse {

    public String type;
    public String category;
    public BigDecimal amount;
    public String recordDate;
    public String remark;
    public Double confidence;
}