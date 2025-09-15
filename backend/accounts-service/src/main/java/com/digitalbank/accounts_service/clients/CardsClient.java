package com.digitalbank.accounts_service.clients;

import java.util.Optional;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.digitalbank.accounts_service.DTO.DebitCardResponse;

@FeignClient(name = "cards-service", path = "/cards")
public interface CardsClient {

    @PostMapping("/debit-card")
    Optional<DebitCardResponse> createDebitCard(@RequestParam("accountNumber") Integer accountNumber);
}