package com.digitalbank.cards_service.services;

import com.digitalbank.cards_service.clients.AccountClient;
import com.digitalbank.cards_service.clients.UserClient;
import com.digitalbank.cards_service.models.DebitCard;
import com.digitalbank.cards_service.repositories.DebitCardRepository;
import com.digitalbank.cards_service.responses.AccountResponse;
import com.digitalbank.cards_service.responses.DebitCardResponse;
import com.digitalbank.cards_service.responses.UserResponse;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class DebitCardService {
    private final DebitCardRepository debitCardRepository;
    private final UserClient userClient;
    private final AccountClient accountClient;
    public DebitCardService(DebitCardRepository debitCardRepository, UserClient userClient, AccountClient accountClient) {
        this.debitCardRepository = debitCardRepository;
        this.userClient = userClient;
        this.accountClient = accountClient;
    }

    @KafkaListener(topics = "account_created", groupId = "card_service_group")
    public ResponseEntity<?> createDebitCard(int accountNumber) {
        AccountResponse account;
        try {
            if(accountClient.findByAccountNumber(accountNumber).isEmpty()) {
                return generateResponse(HttpStatus.NOT_FOUND,"account", "Account not found");
            }
            account = accountClient.findByAccountNumber(accountNumber).get().get(0);
        } catch (NotFoundException e) {
            return generateResponse(HttpStatus.NOT_FOUND, "account", "account with id " + accountNumber + " not found");
        } catch (WebApplicationException e) {
            return generateResponse(HttpStatus.INTERNAL_SERVER_ERROR, "account", "could not connect to account service");
        }

        if(debitCardRepository.findByLinkedAccountNumber(accountNumber).isPresent()) {
            return generateResponse(HttpStatus.CONFLICT, "account", "account already exists");
        }
        DebitCard debitCard = getDebitCard(account);
        DebitCard savedDebitCard = debitCardRepository.save(debitCard);
        return generateCardResponse(savedDebitCard);
    }

    private DebitCard getDebitCard(AccountResponse account) {
        DebitCard debitCard = new DebitCard();
        debitCard.setCreatedOn(new Date());
        debitCard.setCardNumber(generateCardNumber(16));
        debitCard.setExpiryDate(generateExpiryDate(debitCard.getCreatedOn()));
        debitCard.setCvv(generateNumber(3));
        debitCard.setUserId(account.getAccountHolder().getId());
        debitCard.setIsCardActive(true);
        debitCard.setIsCardBlocked(false);
        debitCard.setIsFrozenTemporarily(false);
        debitCard.setCardPin(generateNumber(4));
        debitCard.setLinkedAccountNumber(account.getAccountNumber());
        return debitCard;
    }

    private Date generateExpiryDate(Date createdOn) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(createdOn);
        calendar.add(Calendar.YEAR, 7);
        return resetTime(calendar);
    }

    private Date resetTime(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    private String generateNumber(int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("Length must be positive.");
        }

        long initial = (long) Math.pow(10, length - 1);
        long bound = (long) Math.pow(10, length) - 1;

        long val = ThreadLocalRandom.current().nextLong(initial, bound + 1);
        return Long.toString(val);
    }

    private String generateCardNumber(int totalDigits) {
        if (totalDigits <= 0 || totalDigits % 4 != 0) {
            throw new IllegalArgumentException("Total digits must be a positive multiple of 4.");
        }

        int groupSize = 4;
        int numGroups = totalDigits / groupSize;
        StringBuilder cardNumber = new StringBuilder();

        for (int i = 0; i < numGroups; i++) {
            cardNumber.append(generateNumber(groupSize));
            if (i != numGroups - 1) {
                cardNumber.append("-");
            }
        }

        return cardNumber.toString();
    }


    public ResponseEntity<?> getDebitCard(Long cardId) {
        if(debitCardRepository.existsById(cardId)) {
            return generateCardResponse(debitCardRepository.getReferenceById(cardId));
        }
        return generateResponse(HttpStatus.BAD_REQUEST, "debitCard", "debit card does not exist");
    }

    public ResponseEntity<?> updateDebitCardDetails(Long cardId, DebitCard debitCard) {
        if(debitCardRepository.existsById(cardId)) {
            DebitCard debitCardFromDb = debitCardRepository.getReferenceById(cardId);
            if(debitCard.getCardPin() != null && validPin(debitCard.getCardPin()) && !debitCard.getCardPin().equals(debitCardFromDb.getCardPin())) {
                debitCardFromDb.setCardPin(debitCard.getCardPin());
            }
            if(debitCard.getIsCardActive() != null && !debitCard.getIsCardActive().equals(debitCardFromDb.getIsCardActive())) {
                debitCardFromDb.setIsCardActive(debitCard.getIsCardActive());
            }
            if(debitCard.getIsCardBlocked() != null && !debitCard.getIsCardBlocked().equals(debitCardFromDb.getIsCardBlocked())) {
                debitCardFromDb.setIsCardBlocked(debitCard.getIsCardBlocked());
            }
            if(debitCard.getIsFrozenTemporarily() != null && !debitCard.getIsFrozenTemporarily().equals(debitCardFromDb.getIsFrozenTemporarily())) {
                debitCardFromDb.setIsFrozenTemporarily(debitCard.getIsFrozenTemporarily());
            }
            return generateResponse(HttpStatus.OK, "success", "Debit card details saved successfully");
        }
        return generateResponse(HttpStatus.BAD_REQUEST, "debitCard", "debit card does not exist");
    }

    public ResponseEntity<?> deleteDebitCard(Long cardId) {
        if(debitCardRepository.existsById(cardId)) {
            debitCardRepository.deleteById(cardId);
            return generateResponse(HttpStatus.OK, "success", "Debit card deleted successfully");
        }
        return generateResponse(HttpStatus.BAD_REQUEST, "debitCard", "debit card does not exist");
    }

    private boolean validPin(String cardPin) {
        return cardPin.length() == 4 && cardPin.matches("\\d{4}");
    }

    private ResponseEntity<?> generateResponse(HttpStatus httpStatus, String key, String value) {
        Map<String, String> message = new HashMap<>();
        message.put(key, value);
        return new ResponseEntity<>(message, httpStatus);
    }

    private ResponseEntity<?> generateCardResponse(DebitCard debitCard) {
        DebitCardResponse debitCardResponse = new DebitCardResponse();
        debitCardResponse.setId(debitCard.getId());
        debitCardResponse.setLinkedAccountNumber(debitCard.getLinkedAccountNumber());
        debitCardResponse.setCardNumber(debitCard.getCardNumber());
        debitCardResponse.setExpiryDate(debitCard.getExpiryDate());
        debitCardResponse.setCvv(debitCard.getCvv());
        try {
            debitCardResponse.setCardHolderName(getAccountHolderName(debitCard.getUserId()));
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
        debitCardResponse.setIsCardActive(debitCard.getIsCardActive());
        debitCardResponse.setIsCardBlocked(debitCard.getIsCardBlocked());
        debitCardResponse.setIsFrozenTemporarily(debitCard.getIsFrozenTemporarily());
        return ResponseEntity.ok(debitCardResponse);
    }

    private String getAccountHolderName(Long userId) {
        StringBuilder cardHolderName = new StringBuilder();
        try {
            if(userClient.findById(userId).isEmpty()) {
                System.out.println("No user with user id " + userId);
                throw new NotFoundException("No user with user id " + userId);
            }
            UserResponse user = userClient.findById(userId).get();
            cardHolderName.append(user.getFirstName()).append(" ").append(user.getLastName());
            return cardHolderName.toString();
        } catch (WebApplicationException e) {
            System.out.println("Error Connecting to user service: " + e.getMessage());
        }
        return cardHolderName.toString();
    }
}
