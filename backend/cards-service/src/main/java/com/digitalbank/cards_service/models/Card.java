package com.digitalbank.cards_service.models;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Calendar;
import java.util.Date;

@Data
@MappedSuperclass
public abstract class Card {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String cardNumber;
    @Temporal(TemporalType.DATE)
    private Date expiryDate;
    private String cvv;
    private Long userId;
    private Boolean isCardActive;
    private Boolean isCardBlocked;
    private Boolean isFrozenTemporarily;
    private String cardPin;
    @Temporal(TemporalType.DATE)
    @Column(updatable = false)
    private Date createdOn;
}
