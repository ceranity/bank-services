package com.digitalbank.loan_service.responses;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class LoanResponse {
    private long id;
    private String title;
    private String description;
    private BigDecimal amount;
    private float interestRate;
}
