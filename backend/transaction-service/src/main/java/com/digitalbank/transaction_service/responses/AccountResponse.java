package com.digitalbank.transaction_service.responses;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AccountResponse {
    private Long accountId;
    private int accountNumber;
    private String accountType;
    private String currency;
    private BigDecimal balance;
    private UserResponse accountHolder;
}
