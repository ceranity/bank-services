package com.digitalbank.accounts_service.controllers;

import com.digitalbank.accounts_service.models.Account;
import com.digitalbank.accounts_service.services.AccountService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping
    public ResponseEntity<?> createAccount(@RequestBody Account account) {
        return accountService.createAccount(account);
    }

    @GetMapping
    public ResponseEntity<?> listAccounts() {
        return accountService.getAllAccounts();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getAccountById(@PathVariable Long id) {
        return accountService.getAccountById(id);
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchAccounts(
        @RequestParam(required = false) int accountNumber,
        @RequestParam(required = false) Long customerId,
        @RequestParam(required = false) String accountType,
        @RequestParam(required = false) String currency
    ) {
        System.out.println("Hitting Controller searchAccounts");
        return accountService.searchAccounts(
            accountNumber,
            customerId,
            accountType,
            currency
        );
    }
}
