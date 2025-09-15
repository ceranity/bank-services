package com.digitalbank.transaction_service.controllers;

import com.digitalbank.transaction_service.models.Transaction;
import com.digitalbank.transaction_service.services.TransactionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping("accounts/{accountId}")
    public ResponseEntity<?> getAllTransactionsByAccount(
        @PathVariable Long accountId
    ) {
        return transactionService.listTransactions(accountId);
    }

    @PostMapping
    public ResponseEntity<?> createTransaction(
        @RequestBody Transaction transaction
    ) {
        return transactionService.createTransaction(transaction);
    }

    @PutMapping("/{transactionId}")
    public ResponseEntity<?> updateTransaction(
        @PathVariable Long transactionId,
        @RequestBody Transaction transaction
    ) {
        return transactionService.updateTransaction(transactionId, transaction);
    }
}
