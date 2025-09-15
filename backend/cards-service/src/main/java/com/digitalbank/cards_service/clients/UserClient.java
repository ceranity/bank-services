package com.digitalbank.cards_service.clients;

import com.digitalbank.cards_service.responses.UserResponse;
import java.util.Optional;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service")
public interface UserClient {
    @GetMapping("/users/{id}")
    Optional<UserResponse> findById(@PathVariable("id") Long id);
}
