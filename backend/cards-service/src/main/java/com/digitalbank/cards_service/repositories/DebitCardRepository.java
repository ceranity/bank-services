package com.digitalbank.cards_service.repositories;

import com.digitalbank.cards_service.models.DebitCard;
import com.digitalbank.cards_service.responses.DebitCardResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DebitCardRepository extends JpaRepository<DebitCard, Long> {
    Optional<DebitCard> findByLinkedAccountNumber(int accountNumber);
}
