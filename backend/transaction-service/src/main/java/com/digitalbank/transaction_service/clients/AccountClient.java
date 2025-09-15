package com.digitalbank.transaction_service.clients;

import com.digitalbank.transaction_service.responses.AccountResponse;
import java.util.Optional;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "accounts-service")
public interface AccountClient {
    @GetMapping("/accounts/{id}")
    Optional<AccountResponse> findById(@PathVariable("id") Long id);
}
