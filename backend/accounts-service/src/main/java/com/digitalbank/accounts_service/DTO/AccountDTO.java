package com.digitalbank.accounts_service.DTO;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AccountDTO {
    private Long accountId;
    private int accountNumber;
    private String accountType;
    private String currency;
    private BigDecimal balance;
    private UserDTO accountHolder;
}
