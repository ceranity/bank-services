package com.digitalbank.transaction_service.responses;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TransactionResponse {
    private Long transactionId;
    private BigDecimal amount;
    private String currency;
    private AccountResponse Sender;
    private AccountResponse Receiver;
    private LocalDateTime timestamp;
    private String transactionStatus;
    private String transactionType;
    private String note;
}
