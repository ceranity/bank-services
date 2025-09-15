package com.digitalbank.cards_service.models;

import jakarta.persistence.Entity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Data @EqualsAndHashCode(callSuper=true)
public class DebitCard extends Card {
    private int linkedAccountNumber;
}
