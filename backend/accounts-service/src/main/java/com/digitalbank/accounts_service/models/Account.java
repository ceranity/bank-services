package com.digitalbank.accounts_service.models;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data @Entity
public class Account {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private int accountNumber;
    private Long customerId;
    private String accountType;
    private String currency;
    private BigDecimal balance;
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;
//    ...

}
