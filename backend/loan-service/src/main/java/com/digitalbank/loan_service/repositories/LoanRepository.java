package com.digitalbank.loan_service.repositories;

import com.digitalbank.loan_service.models.Loan;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoanRepository extends JpaRepository<Loan, Long> {
}
