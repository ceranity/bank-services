package com.digitalbank.cards_service.controllers;

import com.digitalbank.cards_service.models.CreditCard;
import com.digitalbank.cards_service.models.DebitCard;
import com.digitalbank.cards_service.services.CreditCardService;
import com.digitalbank.cards_service.services.DebitCardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cards")
public class CardController {

    private final DebitCardService debitCardService;
    private final CreditCardService creditCardService;

    public CardController(
        DebitCardService debitCardService,
        CreditCardService creditCardService
    ) {
        this.debitCardService = debitCardService;
        this.creditCardService = creditCardService;
    }

    @GetMapping("/debit-card/{id}")
    public ResponseEntity<?> getDebitCard(@PathVariable Long id) {
        return debitCardService.getDebitCard(id);
    }

    @PostMapping("/debit-card")
    public ResponseEntity<?> createDebitCard(
        @RequestParam Integer accountNumber
    ) {
        return debitCardService.createDebitCard(accountNumber);
    }

    @PutMapping("/debit-card/{id}")
    public ResponseEntity<?> updateDebitCard(
        @PathVariable Long id,
        @RequestBody DebitCard card
    ) {
        return debitCardService.updateDebitCardDetails(id, card);
    }

    @DeleteMapping("/debit-card/{id}")
    public ResponseEntity<?> deleteDebitCard(@PathVariable Long id) {
        return debitCardService.deleteDebitCard(id);
    }

    @GetMapping("/credit-card/{cardId}")
    public ResponseEntity<?> getCreditCard(@PathVariable Long cardId) {
        return creditCardService.getCreditCardById(cardId);
    }

    @PostMapping("/credit-card")
    public ResponseEntity<?> createCreditCard(
        @RequestParam Long catalogueId,
        @RequestParam Long userId
    ) {
        return creditCardService.createCreditCard(catalogueId, userId);
    }

    @GetMapping("/credit-card/users/{userId}")
    public ResponseEntity<?> getALlCreditCards(@PathVariable Long userId) {
        return creditCardService.listAllCreditCards(userId);
    }

    @PutMapping("/credit-card/{cardId}")
    public ResponseEntity<?> updateCreditCard(
        @PathVariable Long cardId,
        @RequestBody CreditCard card
    ) {
        return creditCardService.updateCreditCard(cardId, card);
    }

    @DeleteMapping("/credit-card/{cardId}")
    public ResponseEntity<?> deleteCreditCard(@PathVariable Long cardId) {
        return creditCardService.deleteCreditCard(cardId);
    }
}
