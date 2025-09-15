package com.digitalbank.auth_service.clients;

import com.digitalbank.auth_service.models.AuthenticationRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Component
@FeignClient(name = "user-service")
public interface UserClient {
    @PostMapping("/users/validate")
    ResponseEntity<?> validateUser(@RequestBody AuthenticationRequest userCredentials);
}
