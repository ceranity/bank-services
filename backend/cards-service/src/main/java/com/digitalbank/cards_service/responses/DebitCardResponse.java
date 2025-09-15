package com.digitalbank.cards_service.responses;

import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.Data;

import java.util.Date;

@Data
public class DebitCardResponse {
    private Long id;
    private int linkedAccountNumber;
    private String cardNumber;
    private Date expiryDate;
    private String cvv;
    private String cardHolderName;
    private Boolean isCardActive;
    private Boolean isCardBlocked;
    private Boolean isFrozenTemporarily;
}
