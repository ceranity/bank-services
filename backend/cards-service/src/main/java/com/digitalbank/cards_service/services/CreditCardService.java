package com.digitalbank.cards_service.services;

import com.digitalbank.cards_service.clients.UserClient;
import com.digitalbank.cards_service.models.CatalogueCreditCard;
import com.digitalbank.cards_service.models.CreditCard;
import com.digitalbank.cards_service.repositories.CatalogueCreditCardRepository;
import com.digitalbank.cards_service.repositories.CreditCardRepository;
import com.digitalbank.cards_service.responses.CreditCardResponse;
import com.digitalbank.cards_service.responses.UserResponse;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class CreditCardService {
    private final CreditCardRepository creditCardRepository;
    private final CatalogueCreditCardRepository catalogueRepo;
    private final UserClient userClient;
    public CreditCardService(CreditCardRepository creditCardRepository, CatalogueCreditCardRepository catalogueRepo, UserClient userClient) {
        this.creditCardRepository = creditCardRepository;
        this.catalogueRepo = catalogueRepo;
        this.userClient = userClient;
    }

    public ResponseEntity<?> createCreditCard(Long catalogueId, Long userId) {
        if(userClient.findById(userId).isEmpty()) {
            return generateResponse(HttpStatus.BAD_REQUEST, "userId","User does not exist");
        }
        if(catalogueRepo.existsById(catalogueId)) {
            CatalogueCreditCard catalogueCard = catalogueRepo.getReferenceById(catalogueId);
            CreditCard creditCard = generateCreditCard(userId, catalogueCard);
            return ResponseEntity.ok(generateCardResponse(creditCardRepository.save(creditCard)));

        }
        return generateResponse(HttpStatus.BAD_REQUEST, "catalogueId", "Card with id "+ catalogueId + " is not offered");
    }

    public ResponseEntity<?> listAllCreditCards(Long userId) {
        if(userClient.findById(userId).isEmpty()) {
            return generateResponse(HttpStatus.BAD_REQUEST, "userId","User does not exist");
        }
        Optional<List<CreditCard>> allCardsByUserId = creditCardRepository.findAllByUserId(userId);
        if(allCardsByUserId.isEmpty()) {
            return generateResponse(HttpStatus.NO_CONTENT, "userId", "User has no credit cards");
        }
        List<CreditCard> cards = allCardsByUserId.get();
        List<CreditCardResponse> creditCardResponses = new ArrayList<>();
        for(CreditCard card : cards) {
            creditCardResponses.add(generateCardResponse(card));
        }
        return ResponseEntity.ok(creditCardResponses);
    }

    public ResponseEntity<?> getCreditCardById(Long creditCardId) {
        if(creditCardId == null) {
            return generateResponse(HttpStatus.BAD_REQUEST, "creditCardId", "Card id cannot be null");
        }
        Optional<CreditCard> creditCard = creditCardRepository.findById(creditCardId);
        if(creditCard.isEmpty()) {
            return generateResponse(HttpStatus.NOT_FOUND, "creditCardId", "Card does not exist");
        }
        return ResponseEntity.ok(generateCardResponse(creditCard.get()));
    }

    public ResponseEntity<?> getCreditCardByNumber(String cardNumber) {
        if(cardNumber == null) {
            return generateResponse(HttpStatus.BAD_REQUEST, "cardNumber", "Card number cannot be null");
        }
        Optional<CreditCard> creditCard = creditCardRepository.findByCardNumber(cardNumber);
        if(creditCard.isEmpty()) {
            return generateResponse(HttpStatus.NOT_FOUND, "cardNumber", "Card does not exist");
        }
        return ResponseEntity.ok(generateCardResponse(creditCard.get()));
    }

    public ResponseEntity<?> updateCreditCard(Long creditCardId, CreditCard creditCard) {
        if(creditCardId == null) {
            return generateResponse(HttpStatus.BAD_REQUEST, "creditCardId", "Card id cannot be null");
        }
        if(creditCard == null) {
            return generateResponse(HttpStatus.BAD_REQUEST, "creditCard", "Card cannot be null");
        }
        if(creditCardRepository.existsById(creditCardId)) {
            CreditCard oldCard = creditCardRepository.getReferenceById(creditCardId);
            oldCard.setIsFrozenTemporarily(creditCard.getIsFrozenTemporarily());
            oldCard.setIsCardBlocked(creditCard.getIsCardBlocked());
            oldCard.setIsCardActive(creditCard.getIsCardActive());
            return ResponseEntity.ok(generateCardResponse(creditCardRepository.save(oldCard)));
        }
        return generateResponse(HttpStatus.NOT_FOUND, "creditCardId", "Card does not exist");
    }

    public ResponseEntity<?> deleteCreditCard(Long creditCardId) {
        if(creditCardId == null) {
            return generateResponse(HttpStatus.BAD_REQUEST, "creditCardId", "Card id cannot be null");
        }
        Optional<CreditCard> creditCard = creditCardRepository.findById(creditCardId);
        if(creditCard.isEmpty()) {
            return generateResponse(HttpStatus.NOT_FOUND, "creditCardId", "Card does not exist");
        }
        creditCardRepository.deleteById(creditCardId);
        return generateResponse(HttpStatus.OK, "creditCardId", "Card deleted successfully");
    }

    private CreditCard generateCreditCard(Long userId, CatalogueCreditCard catalogueCard) {
        CreditCard creditCard = new CreditCard();
        creditCard.setCardNumber(generateCardNumber(16));
        creditCard.setCreatedOn(new Date());
        creditCard.setExpiryDate(generateExpiryDate(creditCard.getCreatedOn()));
        creditCard.setCvv(generateNumber(3));
        creditCard.setUserId(userClient.findById(userId).get().getId());
        creditCard.setIsCardActive(true);
        creditCard.setIsCardBlocked(false);
        creditCard.setIsFrozenTemporarily(false);
        creditCard.setCardPin(generateNumber(4));
        creditCard.setCardName(catalogueCard.getName());
        creditCard.setCardType(catalogueCard.getCardType());
        creditCard.setCreditLimit(catalogueCard.getCreditLimit());
        creditCard.setAvailableLimit(creditCard.getCreditLimit());
        creditCard.setCardClass(catalogueCard.getCardClass());
        creditCard.setBillGenerationDate(generateDate(new Date(), 20, 0));
        creditCard.setBillDueDate(generateDate(creditCard.getBillGenerationDate(), 2, 1));
        creditCard.setBillAmount(BigInteger.ZERO);
        creditCard.setPaymentBalanceAmount(BigInteger.ZERO);
        return creditCard;
    }

    private CreditCardResponse generateCardResponse(CreditCard creditCard) {
        CreditCardResponse cardResponse = new CreditCardResponse();
        cardResponse.setId(creditCard.getId());
        cardResponse.setCardNumber(creditCard.getCardNumber());
        cardResponse.setCardName(creditCard.getCardName());
        cardResponse.setCardType(creditCard.getCardType());
        cardResponse.setExpiryDate(creditCard.getExpiryDate());
        cardResponse.setCvv(creditCard.getCvv());
        cardResponse.setCardHolderName(getAccountHolderName(creditCard.getUserId()));
        cardResponse.setIsCardActive(creditCard.getIsCardActive());
        cardResponse.setIsCardBlocked(creditCard.getIsCardBlocked());
        cardResponse.setIsFrozenTemporarily(creditCard.getIsFrozenTemporarily());
        cardResponse.setCreditLimit(creditCard.getCreditLimit());
        cardResponse.setAvailableLimit(creditCard.getAvailableLimit());
        cardResponse.setCardClass(creditCard.getCardClass());
        cardResponse.setBillGenerationDate(creditCard.getBillGenerationDate());
        cardResponse.setBillAmount(creditCard.getBillAmount());
        cardResponse.setPaymentBalanceAmount(creditCard.getPaymentBalanceAmount());
        return cardResponse;
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

    private Date generateDate(Date createdOn, int day, int addMonth) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(createdOn);
        calendar.add(Calendar.MONTH, addMonth);
        calendar.set(Calendar.DATE, day);
        return resetTime(calendar);
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

    private ResponseEntity<?> generateResponse(HttpStatus httpStatus, String key, String value) {
        Map<String, String> message = new HashMap<>();
        message.put(key, value);
        return new ResponseEntity<>(message, httpStatus);
    }
}
