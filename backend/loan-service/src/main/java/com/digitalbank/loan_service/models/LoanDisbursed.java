package com.digitalbank.loan_service.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.Date;

@Entity
@Data
public class LoanDisbursed {
    @Id
    private Long loanDisbursementId;
    private Long loanId;
    private Long userId;
    private String loanPurpose;
    private String description;
    private Boolean isLoanClosed;
    @Temporal(TemporalType.TIMESTAMP)
    private Date disbursementDate;
    private BigDecimal disbursementAmount;
    private Float disbursementInterest;
    private BigDecimal disbursementAmountPaid;
    @Temporal(TemporalType.DATE)
    private Date nextEmiDate;
    private BigDecimal nextEmiAmount;
    private int tenureMonths;

    @PrePersist
    protected void onCreate() {
        disbursementDate = new Date();
    }
}
