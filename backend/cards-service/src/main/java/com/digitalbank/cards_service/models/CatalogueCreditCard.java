package com.digitalbank.cards_service.models;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigInteger;
import java.util.Date;

@Data @Entity
public class CatalogueCreditCard{
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String cardProvider;
    private String cardType;
    private String cardClass;
    @Temporal(TemporalType.DATE)
    @Column(updatable = false)
    private Date createdOn;
    private int minimumCibilScore;
    private BigInteger creditLimit;
}
