package com.digitalbank.accounts_service.services;

import com.digitalbank.accounts_service.DTO.AccountDTO;
import com.digitalbank.accounts_service.DTO.DebitCardResponse;
import com.digitalbank.accounts_service.DTO.UserDTO;
import com.digitalbank.accounts_service.clients.CardsClient;
import com.digitalbank.accounts_service.clients.UserClient;
import com.digitalbank.accounts_service.models.Account;
import com.digitalbank.accounts_service.repositories.AccountRepository;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import java.math.BigDecimal;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final UserClient userClient;
    private final CardsClient cardsClient;

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String TOPIC = "account_created";

    public AccountService(
            AccountRepository accountRepository,
            UserClient userClient,
            CardsClient cardsClient,
            KafkaTemplate<String,Object> kafkaTemplate) {
        this.accountRepository = accountRepository;
        this.userClient = userClient;
        this.cardsClient = cardsClient;
        this.kafkaTemplate = kafkaTemplate;
    }

    public ResponseEntity<?> createAccount(Account account) {
        Map<String, String> errors = validateAccount(account);
        if (!errors.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
        }

        UserDTO userDTO = fetchUserById(account.getCustomerId());
        if (userDTO == null || userDTO.getId() == null) {
            errors.put("customerId", "The customer ID is not valid");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
        }

        setupAccountDefaults(account);
        account.setAccountNumber(generateUniqueAccountNumber());

        Account registeredAccount = accountRepository.save(account);
        AccountDTO accountDTO = mapToAccountDTO(registeredAccount, userDTO);
        // initiate trigger to create a debit card in cards-service
        try {
            kafkaTemplate.send(TOPIC, "AccountCreated", accountDTO.getAccountNumber());
        } catch (Exception e) {
            System.out.println("Could not create Debit Card. " + e);
        }
        return ResponseEntity.ok(accountDTO);
    }

    private Map<String, String> validateAccount(Account account) {
        Map<String, String> errors = new HashMap<>();

        if (account.getCustomerId() == null ||
                account.getCustomerId().longValue() <= 0) {
            errors.put(
                    "customerId",
                    "The customer ID is mandatory and must be valid");
        }

        if (account.getAccountType() == null ||
                account.getAccountType().isEmpty()) {
            errors.put(
                    "accountType",
                    "The account type is mandatory and must be valid");
        }

        if (account.getCurrency() != null && account.getCurrency().isEmpty()) {
            errors.put("currency", "The currency must be valid");
        }

        return errors;
    }

    private UserDTO fetchUserById(Long customerId) {
        try {
            return userClient.findById(customerId);
        } catch (BadRequestException e) {
            throw new BadRequestException(
                    "Improper request. Please check the customer ID for Account service.");
        } catch (NotFoundException e) {
            throw new NotFoundException(
                    "Requested customer not found in Account service.");
        } catch (WebApplicationException e) {
            throw new WebApplicationException(
                    "Request failed. Please check the request for Account service.");
        } catch (Exception e) {
            throw new RuntimeException("Something went wrong!");
        }
    }

    private void setupAccountDefaults(Account account) {
        if (account.getCurrency() == null) {
            account.setCurrency("INR");
        }

        if (account.getBalance() == null) {
            account.setBalance(BigDecimal.ZERO);
        }
    }

    private AccountDTO mapToAccountDTO(Account account, UserDTO userDTO) {
        AccountDTO accountDTO = new AccountDTO();
        accountDTO.setAccountId(account.getId());
        accountDTO.setAccountType(account.getAccountType());
        accountDTO.setCurrency(account.getCurrency());
        accountDTO.setBalance(account.getBalance());
        accountDTO.setAccountNumber(account.getAccountNumber());
        accountDTO.setAccountHolder(userDTO);
        return accountDTO;
    }

    private int generateUniqueAccountNumber() {
        Random random = new Random();
        int accountNumber;
        do {
            accountNumber = 10000000 + random.nextInt(90000000); // 8-digit account number
        } while (accountRepository.existsByAccountNumber(accountNumber));
        return accountNumber;
    }

    public ResponseEntity<?> updateAccount(Long id, Account newAccount) {
        Optional<Account> existingAccount = accountRepository.findById(id);
        if (existingAccount.isPresent()) {
            Account accountToUpdate = existingAccount.get();
            accountToUpdate.setAccountType(newAccount.getAccountType());
            accountToUpdate.setBalance(newAccount.getBalance());
            accountToUpdate.setCurrency(newAccount.getCurrency());
            accountRepository.save(accountToUpdate);
            return ResponseEntity.ok(accountToUpdate);
        } else {
            Map<String, String> error = new HashMap<>();
            error.put("error", "No account found");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    public ResponseEntity<?> deleteAccount(Long id) {
        if (accountRepository.existsById(id)) {
            accountRepository.deleteById(id);
            Map<String, String> message = new HashMap<>();
            message.put("message", "Account deleted successfully");
            return ResponseEntity.ok(message);
        } else {
            Map<String, String> error = new HashMap<>();
            error.put("error", "No account found");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    public ResponseEntity<?> getAllAccounts() {
        List<Account> accounts = accountRepository.findAll();
        if (accounts.isEmpty()) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "No accounts found");
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(response);
        }

        List<AccountDTO> accountDTOs = new ArrayList<>();
        for (Account account : accounts) {
            UserDTO accountHolder = userClient.findById(
                    account.getCustomerId());
            AccountDTO accountDTO = mapToAccountDTO(account, accountHolder);
            accountDTOs.add(accountDTO);
        }

        return ResponseEntity.ok(accountDTOs);
    }

    public ResponseEntity<?> getAccountById(Long id) {
        Optional<Account> account = accountRepository.findById(id);
        if (account.isPresent()) {
            UserDTO accountHolder = userClient.findById(
                    account.get().getCustomerId());
            AccountDTO accountDTO = mapToAccountDTO(
                    account.get(),
                    accountHolder);
            return ResponseEntity.ok(accountDTO);
        } else {
            Map<String, String> error = new HashMap<>();
            error.put("error", "No account found");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    public ResponseEntity<?> getAccountByAccountNumber(int accountNumber) {
        Optional<Account> account = accountRepository.findByAccountNumber(
                accountNumber);
        if (account.isPresent()) {
            UserDTO accountHolder = userClient.findById(
                    account.get().getCustomerId());
            AccountDTO accountDTO = mapToAccountDTO(
                    account.get(),
                    accountHolder);
            return ResponseEntity.ok(accountDTO);
        } else {
            Map<String, String> error = new HashMap<>();
            error.put("error", "No account found");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    public ResponseEntity<?> searchAccounts(
            String token,
            int accountNumber,
            Long customerId,
            String accountType,
            String currency) {
        List<Account> accounts = accountRepository.findAll();
        if (accounts.isEmpty()) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "No accounts found");
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(response);
        }

        List<AccountDTO> accountDTOs = new ArrayList<>();
        for (Account account : accounts) {
            if (accountNumber != 0 &&
                    account.getAccountNumber() != accountNumber) {
                System.out.println(
                        "Hitting Service Account Num. : " + accountNumber);
                continue;
            }
            if (customerId != null &&
                    !account.getCustomerId().equals(customerId)) {
                continue;
            }
            if (accountType != null &&
                    !account.getAccountType().equals(accountType)) {
                continue;
            }
            if (currency != null && !account.getCurrency().equals(currency)) {
                continue;
            }

            UserDTO accountHolder = userClient.findById(
                    account.getCustomerId());
            AccountDTO accountDTO = mapToAccountDTO(account, accountHolder);
            accountDTOs.add(accountDTO);
        }

        return ResponseEntity.ok(accountDTOs);
    }
}
