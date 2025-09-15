package com.digitalbank.cards_service.repositories;

import com.digitalbank.cards_service.models.CreditCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CreditCardRepository extends JpaRepository<CreditCard, Long> {
    Optional<List<CreditCard>> findAllByUserId(Long userId);

    Optional<CreditCard> findByCardNumber(String cardNumber);
}
