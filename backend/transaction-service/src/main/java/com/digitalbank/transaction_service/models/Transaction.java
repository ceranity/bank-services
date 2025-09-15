package com.digitalbank.transaction_service.models;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

@Data @Entity
public class Transaction {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long senderId;
    private Long recipientId;
    private String currency;
    private BigDecimal amount;
    @Temporal(TemporalType.TIMESTAMP)
    private Date timestamp;
    private String transactionStatus;
    private String transactionType;
    private String note;
}
