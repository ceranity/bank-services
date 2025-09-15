package com.digitalbank.loan_service.services;

import com.digitalbank.loan_service.clients.UserClient;
import com.digitalbank.loan_service.models.Loan;
import com.digitalbank.loan_service.models.LoanDisbursed;
import com.digitalbank.loan_service.repositories.LoanDisbursedRepository;
import com.digitalbank.loan_service.repositories.LoanRepository;
import com.digitalbank.loan_service.responses.LoanResponse;
import com.digitalbank.loan_service.responses.UserResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Service
public class LoanService {
    private final LoanRepository loanRepository;
    private final LoanDisbursedRepository loanDisbursedRepository;
    private final UserClient userClient;

    public LoanService(LoanRepository loanRepository, LoanDisbursedRepository loanDisbursedRepository, UserClient userClient) {
        this.loanRepository = loanRepository;
        this.loanDisbursedRepository = loanDisbursedRepository;
        this.userClient = userClient;
    }

    public ResponseEntity<?> getAllLoans() {
        if(loanRepository.findAll().isEmpty()) {
            return callResponse(HttpStatus.NO_CONTENT, "message", "No Loans available now. Please try later.");
        }
        return new ResponseEntity<>(loanRepository.findAll(), HttpStatus.OK);
    }

    public ResponseEntity<?> getLoan(Long id) {
        if(!loanRepository.existsById(id)) {
            return callResponse(HttpStatus.BAD_REQUEST, "id", "Loan with id "+ id +" does not exist.");
        }
        Loan loan = loanRepository.getReferenceById(id);
        return ResponseEntity.ok(convertToDTO(loan));
    }

    public ResponseEntity<?> createLoan(Loan loan) {
        if(loan == null) {
            return callResponse(HttpStatus.BAD_REQUEST, "loan", "Please provide Loan details.");
        }
        if(loan.getTitle() == null || loan.getTitle().isEmpty()) {
            return callResponse(HttpStatus.BAD_REQUEST, "title", "Title cannot be empty.");
        }
        if(loan.getAmount() == null || loan.getAmount().doubleValue() <= 0) {
            return callResponse(HttpStatus.BAD_REQUEST, "amount", "Amount cannot be empty or invalid.");
        }
        if(loan.getInterestRate() <= 0) {
            return callResponse(HttpStatus.BAD_REQUEST, "interestRate", "Interest rate cannot be empty or invalid.");
        }
        loanRepository.save(loan);
        return ResponseEntity.ok(convertToDTO(loan));
    }

    private Long generateLoanId() {
        Random random = new Random();
        long loanId;
        do {
            loanId = random.nextLong(10000, 999999);
        } while (loanDisbursedRepository.existsById(loanId));
        return Long.valueOf(loanId);
    }

    public ResponseEntity<?> updateLoan(Long id, Loan loan) {
        if(!loanRepository.existsById(id)) {
            return callResponse(HttpStatus.BAD_REQUEST, "id", "Loan with id "+ id +" does not exist.");
        }
        Loan updatedLoan = loanRepository.getReferenceById(id);
        updatedLoan.setTitle(loan.getTitle());
        updatedLoan.setAmount(loan.getAmount());
        updatedLoan.setInterestRate(loan.getInterestRate());
        loanRepository.save(updatedLoan);
        return ResponseEntity.ok(convertToDTO(updatedLoan));
    }

    public ResponseEntity<?> deleteLoan(Long id) {
        if(!loanRepository.existsById(id)) {
            return callResponse(HttpStatus.BAD_REQUEST, "id", "Loan with id "+ id +" does not exist.");
        }
        loanRepository.deleteById(id);
        return callResponse(HttpStatus.OK, "message", "Loan with id "+ id +" has been deleted.");
    }

    public ResponseEntity<?> applyForLoan(Long userId, Long loanId, int tenureMonths) {
        if(!loanRepository.existsById(loanId)) {
            return callResponse(HttpStatus.BAD_REQUEST, "id", "Loan with id "+ loanId + " does not exist.");
        }
        if(userClient.findById(userId) == null) {
            return callResponse(HttpStatus.BAD_REQUEST, "userId", "User with id "+ userId + " does not exist.");
        }
        if(tenureMonths <= 0) {
            return callResponse(HttpStatus.BAD_REQUEST, "tenure", "Tenure cannot be empty or less than zero.");
        } else if(tenureMonths > 108) {
            return callResponse(HttpStatus.BAD_REQUEST, "tenure", "Tenure cannot be more than 108 months");
        }
        Loan loan = loanRepository.getReferenceById(loanId);
        UserResponse user = userClient.findById(userId);
//        check for user eligibility
//        isUserEligibleForLoan(user);
        Long loanDisbursementId = generateLoanId();
        LoanDisbursed loanToDisburse = new LoanDisbursed();
        loanToDisburse.setLoanDisbursementId(loanDisbursementId);
        loanToDisburse.setUserId(userId);
        loanToDisburse.setLoanId(loan.getId());
        loanToDisburse.setDisbursementAmount(loan.getAmount());
        loanToDisburse.setDisbursementAmountPaid(BigDecimal.ZERO);
        loanToDisburse.setDisbursementInterest(loan.getInterestRate());
        loanToDisburse.setLoanPurpose(loan.getTitle());
        loanToDisburse.setDescription(loan.getDescription());
        loanToDisburse.setIsLoanClosed(Boolean.FALSE);
        loanToDisburse.setTenureMonths(tenureMonths);

        loanToDisburse.setNextEmiDate(calculateNextEMIDate(new Date()));
        loanToDisburse.setNextEmiAmount(calculateNextEMIAmount(loan.getAmount(), loan.getInterestRate(), tenureMonths));
        LoanDisbursed loanDisbursed = loanDisbursedRepository.save(loanToDisburse);
        return ResponseEntity.ok(loanDisbursed);
    }

    public ResponseEntity<?> fetchUserLoans(Long userId) {
        if(userClient.findById(userId) == null ) {
            return callResponse(HttpStatus.BAD_REQUEST, "userId", "User with id "+ userId + " does not exist.");
        }
        Optional<List<LoanDisbursed>> loans = loanDisbursedRepository.findByUserId(userId);
        if(loans.isPresent()) {
            return ResponseEntity.ok(loans.get());
        }
        return callResponse(HttpStatus.NO_CONTENT, "loans", "User has no loans");
    }

    private BigDecimal calculateNextEMIAmount(BigDecimal amount, float interestRate, int tenureMonths) {
        if(amount.equals(BigDecimal.ZERO) || tenureMonths == 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal interestAmount = amount.multiply(BigDecimal.valueOf(interestRate / 100));
        BigDecimal totalAmount = amount.add(interestAmount);
        BigDecimal emiAmount = amount.divide(BigDecimal.valueOf(tenureMonths), RoundingMode.CEILING);
        return emiAmount.add(totalAmount).setScale(2, RoundingMode.CEILING);
    }

    private Date calculateNextEMIDate(Date date) {
//        Pay EMI on 2nd of every month
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        if(dayOfMonth > 20) {
            calendar.add(Calendar.MONTH, 2);
        }
        else {
            calendar.add(Calendar.MONTH, 1);
        }
        calendar.set(Calendar.DAY_OF_MONTH, 2);

        // Reset the time part to midnight (00:00:00)
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar.getTime();
    }

    private LoanResponse convertToDTO(Loan loan) {
        LoanResponse loanResponse = new LoanResponse();
        loanResponse.setId(loan.getId());
        loanResponse.setAmount(loan.getAmount());
        loanResponse.setTitle(loan.getTitle());
        loanResponse.setDescription(loan.getDescription());
        loanResponse.setInterestRate(loan.getInterestRate());
        return loanResponse;
    }

    private ResponseEntity<?> callResponse(HttpStatus httpStatus, String key, String value) {
        Map<String, String> message = new HashMap<>();
        message.put(key, value);
        return ResponseEntity.status(httpStatus).body(message);
    }
}
