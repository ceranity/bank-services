package com.digitalbank.transaction_service.services;

import com.digitalbank.transaction_service.clients.AccountClient;
import com.digitalbank.transaction_service.models.Transaction;
import com.digitalbank.transaction_service.repositories.TransactionRepository;
import com.digitalbank.transaction_service.responses.AccountResponse;
import com.digitalbank.transaction_service.responses.TransactionResponse;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

@Service
public class TransactionService {
    private final TransactionRepository transactionRepo;
    private final AccountClient accountClient;

    public TransactionService(TransactionRepository transactionRepository, AccountClient accountClient) {
        this.transactionRepo= transactionRepository;
        this.accountClient = accountClient;
    }

    public ResponseEntity<?> createTransaction(Transaction transaction) {
        if(transaction.getSenderId() == null) {
            return generateResponse(HttpStatus.BAD_REQUEST, "senderId", "senderId cannot be null");
        }
        if(transaction.getRecipientId() == null) {
            return generateResponse(HttpStatus.BAD_REQUEST, "recipientId", "recipientId cannot be null");
        }

        if(accountClient.findById(transaction.getSenderId()).isEmpty()) {
            return generateResponse(HttpStatus.BAD_REQUEST, "senderId", "User Account with senderId " + transaction.getSenderId() + " does not exist");
        }
        if(accountClient.findById(transaction.getRecipientId()).isEmpty()) {
            return generateResponse(HttpStatus.BAD_REQUEST, "recipientId", "User Account with recipientId " + transaction.getRecipientId() + " does not exist");
        }
        if(transaction.getCurrency() == null || transaction.getCurrency().isEmpty()) {
            return generateResponse(HttpStatus.BAD_REQUEST, "currency", "Currency cannot be empty");
        }
        BigDecimal amount = transaction.getAmount();
        if(amount == null) {
            return generateResponse(HttpStatus.BAD_REQUEST, "amount", "Amount cannot be empty");
        } else if(amount.compareTo(BigDecimal.ZERO) <= 0) {
            return generateResponse(HttpStatus.BAD_REQUEST, "amount", "Amount must be greater than 0");
        }
        if(transaction.getTransactionType() == null) {
            transaction.setTransactionType("OTHERS");
        }
        transaction.setTransactionStatus("pending");
        String transactionStatus = sendAmountToRecipient(transaction.getSenderId(), transaction.getRecipientId(), transaction.getAmount());
        switch (transactionStatus) {
            case "pending" -> transaction.setTransactionStatus("pending");
            case "completed" -> transaction.setTransactionStatus("completed");
            case "failed" -> transaction.setTransactionStatus("failed");
        }
        transaction.setTimestamp(new Date());
        TransactionResponse transactionResponse;
        try {
            transactionResponse = convertToResponse(transactionRepo.save(transaction));
        } catch (NotFoundException e) {
            return generateResponse(HttpStatus.BAD_REQUEST, "error", e.getMessage());
        } catch (Exception e) {
            return generateResponse(HttpStatus.INTERNAL_SERVER_ERROR, "error", e.getMessage());
        }
        return ResponseEntity.ok(transactionResponse);
    }

    public ResponseEntity<?> listTransactions(Long accountId) {
        if(accountId == null) {
            return generateResponse(HttpStatus.BAD_REQUEST, "accountId", "AccountId cannot be null");
        } else if (accountClient.findById(accountId).isEmpty()) {
            return generateResponse(HttpStatus.BAD_REQUEST, "accountId", "AccountId " + accountId + " does not exist");
        }
        Optional<List<Transaction>> transactions = transactionRepo.findAllBySenderIdOrRecipientId(accountId, accountId);
        if(transactions.isEmpty()) {
            return generateResponse(HttpStatus.NO_CONTENT, "accountId", "AccountId " + accountId + " does not have any transactions");
        }
        List<TransactionResponse> transactionResponses = new ArrayList<>();
        for(Transaction transaction : transactions.get()) {
            transactionResponses.add(convertToResponse(transaction));
        }
        return ResponseEntity.ok(transactionResponses);
    }

    public ResponseEntity<?> updateTransaction(Long id, Transaction transaction) {
        if(!transactionRepo.existsById(id)) {
            return generateResponse(HttpStatus.BAD_REQUEST, "id", "Transaction with id " + id + " does not exist");
        }
        Transaction oldTransaction = transactionRepo.getReferenceById(id);
        if(!oldTransaction.getTransactionStatus().equals(transaction.getTransactionStatus())) {
            oldTransaction.setTransactionStatus(transaction.getTransactionStatus());
            return ResponseEntity.ok(convertToResponse(transactionRepo.save(oldTransaction)));
        }
        return generateResponse(HttpStatus.OK, "id", "Transaction with id " + id + " has been updated");
    }

    private TransactionResponse convertToResponse(Transaction transaction) {
        TransactionResponse transactionResponse = new TransactionResponse();
        transactionResponse.setTransactionId(transaction.getId());
        transactionResponse.setTransactionType(transaction.getTransactionType());
        transactionResponse.setAmount(transaction.getAmount());
        transactionResponse.setCurrency(transaction.getCurrency());
        transactionResponse.setTransactionStatus(transaction.getTransactionStatus());
        try {
            transactionResponse.setSender(accountClient.findById(transaction.getSenderId()).orElse(null));
        } catch (Exception e) {
            throw new NotFoundException("Sender could not be found");
        }
        try {
            transactionResponse.setReceiver(accountClient.findById(transaction.getRecipientId()).orElse(null));
        } catch (Exception e) {
            throw new NotFoundException("Recipient could not be found");
        }
        return transactionResponse;
    }

    private String sendAmountToRecipient(Long senderId, Long recipientId, BigDecimal amount) {
//        TODO - implement sending and receiving money
        AccountResponse senderAccount = accountClient.findById(senderId).orElse(null);
        AccountResponse recipientAccount = accountClient.findById(recipientId).orElse(null);
        if(senderAccount == null || recipientAccount == null) {
            throw new NotFoundException("Sender could not be found");
        }
        try {
//            TODO - implement put method in account service
            senderAccount.setBalance(senderAccount.getBalance().subtract(amount));
            recipientAccount.setBalance(recipientAccount.getBalance().add(amount));
        } catch (WebApplicationException e) {
            return "pending";
        } catch (Exception e) {
            return "failed";
        }
        return "completed";
    }

    private ResponseEntity<?> generateResponse(HttpStatus httpStatus, String key, String value) {
        Map<String, String> message = new HashMap<>();
        message.put(key, value);
        return new ResponseEntity<>(message, httpStatus);
    }
}
