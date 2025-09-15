package com.digitalbank.cards_service.responses;

import lombok.Data;

@Data
public class CatalogueCreditCardResponse {
    private Long id;
    private String name;
    private String cardProvider;
    private String cardType;
    private String cardClass;
}
