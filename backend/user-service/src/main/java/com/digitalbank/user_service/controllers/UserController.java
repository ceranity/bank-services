package com.digitalbank.user_service.controllers;

import com.digitalbank.user_service.responses.AuthenticationResponse;
import com.digitalbank.user_service.responses.UserResponse;
import com.digitalbank.user_service.models.AuthenticationRequest;
import com.digitalbank.user_service.models.User;
import com.digitalbank.user_service.services.JwtService;
import com.digitalbank.user_service.services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final JwtService jwtService;

    public UserController(UserService userService, JwtService jwtService) {
        this.userService = userService;
        this.jwtService = jwtService;
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody User user) {
        try {
            return ResponseEntity.ok(userService.createUser(user));
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(@RequestBody AuthenticationRequest authenticationRequest) {
        User authenticatedUser = userService.authenticate(authenticationRequest);

        String jwtToken = jwtService.generateToken(authenticatedUser);
        AuthenticationResponse authResponse = new AuthenticationResponse();
        authResponse.setToken(jwtToken);
        authResponse.setExpiresIn(jwtService.getExpirationTime(jwtToken));

        return ResponseEntity.ok(authResponse);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUser(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @GetMapping("/{id}/change-username")
    public ResponseEntity<UserResponse> changeUsername(@PathVariable Long id) {
        return ResponseEntity.ok(userService.changeUsername(id));
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchUser(
            @RequestParam(required = false) String emailId,
            @RequestParam(required = false) String phoneNumber,
            @RequestParam(required = false) String username) {
        if (emailId != null) {
            return ResponseEntity.ok(userService.getUserByEmail(emailId));
        } else if (phoneNumber != null) {
            return ResponseEntity.ok(userService.getUserByPhone(phoneNumber));
        } else if (username != null) {
            return ResponseEntity.ok(userService.getUserByUsername(username));
        } else {
            Map<String, String> errors = new HashMap<>();
            errors.put("requestParam", "Request parameter invalid.");
            return ResponseEntity.badRequest().body(errors);
        }
    }
}
