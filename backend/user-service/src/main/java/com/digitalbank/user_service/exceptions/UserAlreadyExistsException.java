package com.digitalbank.user_service.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.Map;

@Getter
@ResponseStatus(HttpStatus.CONFLICT) // This annotation sets the response status for this exception
public class UserAlreadyExistsException extends RuntimeException {

    private final Map<String, String> errorDetails;

    public UserAlreadyExistsException() {
        super("User already exists with provided details.");
        this.errorDetails = null;
    }

    public UserAlreadyExistsException(String message) {
        super(message);
        this.errorDetails = null;
    }

    public UserAlreadyExistsException(String message, Map<String, String> errorDetails) {
        super(message);
        this.errorDetails = errorDetails;
    }

}
