package com.digitalbank.cards_service.responses;

import lombok.Data;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

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
