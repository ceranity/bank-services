package com.digitalbank.auth_service.responses;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @AllArgsConstructor
public class AuthenticationResponse {
    private final String token;
}
