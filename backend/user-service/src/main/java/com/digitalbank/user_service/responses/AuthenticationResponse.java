package com.digitalbank.user_service.responses;

import lombok.Data;

@Data
public class AuthenticationResponse {
    private String token;
    private long expiresIn;
}
