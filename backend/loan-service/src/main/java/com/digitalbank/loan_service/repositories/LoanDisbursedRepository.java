package com.digitalbank.loan_service.repositories;

import com.digitalbank.loan_service.models.LoanDisbursed;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LoanDisbursedRepository extends JpaRepository<LoanDisbursed, Long> {
    Optional<List<LoanDisbursed>> findByUserId(Long userId);
}
