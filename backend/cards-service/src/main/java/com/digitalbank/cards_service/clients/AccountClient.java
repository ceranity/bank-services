package com.digitalbank.cards_service.clients;

import com.digitalbank.cards_service.responses.AccountResponse;
import java.util.List;
import java.util.Optional;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "accounts-service")
public interface AccountClient {
    @GetMapping("/accounts/{id}")
    Optional<AccountResponse> findById(@PathVariable("id") Long id);

    @GetMapping("/accounts/search")
    Optional<List<AccountResponse>> findByAccountNumber(
        @RequestParam("accountNumber") Integer accountNumber
    );
}
