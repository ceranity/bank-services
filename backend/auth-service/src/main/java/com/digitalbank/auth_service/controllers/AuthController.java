package com.digitalbank.auth_service.controllers;

import com.digitalbank.auth_service.clients.UserClient;
import com.digitalbank.auth_service.models.AuthenticationRequest;
import com.digitalbank.auth_service.services.AuthService;
import com.digitalbank.auth_service.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody AuthenticationRequest request) {
        return authService.createAuthenticationToken(request);
    }
}
