package com.digitalbank.transaction_service.repositories;

import com.digitalbank.transaction_service.models.Transaction;
import com.digitalbank.transaction_service.responses.AccountResponse;
import com.digitalbank.transaction_service.responses.TransactionResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    Optional<List<Transaction>> findAllBySenderIdOrRecipientId(Long senderId, Long recipientId);
}
