package com.digitalbank.cards_service.controllers;

import com.digitalbank.cards_service.models.CatalogueCreditCard;
import com.digitalbank.cards_service.services.CatalogueCreditCardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/credit-cards")
public class CatalogueCreditCardController {

    private final CatalogueCreditCardService catalogueCreditCardService;

    public CatalogueCreditCardController(
        CatalogueCreditCardService catalogueCreditCardService
    ) {
        this.catalogueCreditCardService = catalogueCreditCardService;
    }

    @GetMapping
    public ResponseEntity<?> getAllCreditCards() {
        return catalogueCreditCardService.listAllCreditCards();
    }

    @PostMapping
    public ResponseEntity<?> createCreditCard(
        @RequestBody CatalogueCreditCard creditCard
    ) {
        return catalogueCreditCardService.addCreditCard(creditCard);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getCreditCardById(@PathVariable Long id) {
        return catalogueCreditCardService.fetchCreditCardById(id);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateCreditCard(
        @PathVariable Long id,
        @RequestBody CatalogueCreditCard creditCard
    ) {
        return catalogueCreditCardService.updateCreditCard(id, creditCard);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCreditCard(@PathVariable Long id) {
        return catalogueCreditCardService.deleteCreditCard(id);
    }
}
