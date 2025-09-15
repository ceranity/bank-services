package com.digitalbank.cards_service.services;

import com.digitalbank.cards_service.models.CatalogueCreditCard;
import com.digitalbank.cards_service.models.CreditCard;
import com.digitalbank.cards_service.repositories.CatalogueCreditCardRepository;
import com.digitalbank.cards_service.responses.CatalogueCreditCardResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CatalogueCreditCardService {
    private final CatalogueCreditCardRepository catalogueCreditCardRepo;
    public CatalogueCreditCardService(CatalogueCreditCardRepository catalogueCreditCardRepo) {
        this.catalogueCreditCardRepo = catalogueCreditCardRepo;
    }

    public ResponseEntity<?> addCreditCard(CatalogueCreditCard creditCard) {
//        System.out.println(creditCard);
        if(creditCard.getName() == null || creditCard.getName().isEmpty()) {
            return generateResponse(HttpStatus.BAD_REQUEST, "name", "credit card name cannot be empty");
        }
        if(creditCard.getCardType() == null || creditCard.getCardType().isEmpty()) {
            return generateResponse(HttpStatus.BAD_REQUEST, "cardType", "card type cannot be empty");
        }
        if(creditCard.getCardProvider() == null || creditCard.getCardProvider().isEmpty()) {
            return generateResponse(HttpStatus.BAD_REQUEST, "cardProvider", "card provider cannot be empty");
        }
        if(creditCard.getCardClass() == null || creditCard.getCardClass().isEmpty()) {
            creditCard.setCardClass("DEFAULT");
        }
        creditCard.setCreatedOn(new Date());
        if(creditCard.getMinimumCibilScore() <= 0) {
            creditCard.setMinimumCibilScore(670);
        }
        if(creditCard.getCreditLimit() == null || creditCard.getCreditLimit().compareTo(BigInteger.ZERO) <= 0) {
            creditCard.setCreditLimit(BigInteger.valueOf(25000));
        }
        catalogueCreditCardRepo.save(creditCard);
        return ResponseEntity.ok(creditCard);
    }

    public ResponseEntity<?> listAllCreditCards() {
        List<CatalogueCreditCard> creditCards = catalogueCreditCardRepo.findAll();
        if(creditCards.isEmpty()) {
            return generateResponse(HttpStatus.NO_CONTENT, "creditCards", "No creditCards found.");
        }
        List<CatalogueCreditCardResponse> cards = creditCards.stream()
                .map(this::generateCardResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(cards);
    }

    public ResponseEntity<?> fetchCreditCardById(Long id) {
        if(catalogueCreditCardRepo.existsById(id)) {
            CatalogueCreditCard creditCard = catalogueCreditCardRepo.getReferenceById(id);
            return ResponseEntity.ok(generateCardResponse(creditCard));
        }
        return generateResponse(HttpStatus.NOT_FOUND, "creditCard", "No creditCard found.");
    }

    public ResponseEntity<?> updateCreditCard(Long id, CatalogueCreditCard creditCard) {
        if(!catalogueCreditCardRepo.existsById(id)) {
            return generateResponse(HttpStatus.BAD_REQUEST, "id", "creditCard with id " + id + " does not exist");
        }
        CatalogueCreditCard oldCard = catalogueCreditCardRepo.getReferenceById(id);
        oldCard.setName(creditCard.getName());
        oldCard.setCardType(creditCard.getCardType());
        oldCard.setCardProvider(creditCard.getCardProvider());
        oldCard.setCardClass(creditCard.getCardClass());

        return ResponseEntity.ok(generateCardResponse(oldCard));
    }

    public ResponseEntity<?> deleteCreditCard(Long id) {
        if(!catalogueCreditCardRepo.existsById(id)) {
            return generateResponse(HttpStatus.BAD_REQUEST, "id", "creditCard with id " + id + " does not exist");
        }
        CatalogueCreditCard creditCard = catalogueCreditCardRepo.getReferenceById(id);
        catalogueCreditCardRepo.delete(creditCard);
        return generateResponse(HttpStatus.OK, "message", "Credit card with id " + id + " was deleted.");
    }

    private CatalogueCreditCardResponse generateCardResponse(CatalogueCreditCard catalogueCard) {
        CatalogueCreditCardResponse responseCard = new CatalogueCreditCardResponse();
        responseCard.setId(catalogueCard.getId());
        responseCard.setName(catalogueCard.getName());
        responseCard.setCardType(catalogueCard.getCardType());
        responseCard.setCardProvider(catalogueCard.getCardProvider());
        responseCard.setCardClass(catalogueCard.getCardClass());
        return responseCard;
    }


    private ResponseEntity<?> generateResponse(HttpStatus httpStatus, String key, String value) {
        Map<String, String> message = new HashMap<>();
        message.put(key, value);
        return new ResponseEntity<>(message, httpStatus);
    }

}
