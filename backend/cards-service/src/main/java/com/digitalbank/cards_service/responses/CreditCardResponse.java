package com.digitalbank.cards_service.responses;

import lombok.Data;

import java.math.BigInteger;
import java.util.Date;

@Data
public class CreditCardResponse {
    private Long id;
    private String cardNumber;
    private String cardName;
    private String cardType;
    private Date expiryDate;
    private String cvv;
    private String cardHolderName;
    private Boolean isCardActive;
    private Boolean isCardBlocked;
    private Boolean isFrozenTemporarily;
    private BigInteger creditLimit;
    private BigInteger availableLimit;
    private String cardClass;
    private Date billGenerationDate;
    private BigInteger billAmount;
    private BigInteger paymentBalanceAmount;
}
