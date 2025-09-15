package com.digitalbank.auth_service.services;

import com.digitalbank.auth_service.clients.UserClient;
import com.digitalbank.auth_service.models.AuthenticationRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private UserClient userClient;

    public ResponseEntity<?> createAuthenticationToken(AuthenticationRequest request) {
        ResponseEntity<?> responseEntity = userClient.validateUser(request);
        if (responseEntity.getStatusCode().is2xxSuccessful()) {
        //            generate token
            final UserDetails userDetails = request.getUsername();
        } else {
            return new ResponseEntity<>(responseEntity.getBody(), responseEntity.getStatusCode());
        }
    }
}
