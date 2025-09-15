package com.digitalbank.user_service.services;

import com.digitalbank.user_service.responses.UserResponse;
import com.digitalbank.user_service.clients.AgentNameClient;
import com.digitalbank.user_service.exceptions.ResourceNotFoundException;
import com.digitalbank.user_service.exceptions.UserAlreadyExistsException;
import com.digitalbank.user_service.models.AgentName;
import com.digitalbank.user_service.models.AuthenticationRequest;
import com.digitalbank.user_service.models.User;
import com.digitalbank.user_service.repositories.UserRepository;
import com.digitalbank.user_service.security.CustomAuthenticationProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final CustomAuthenticationProvider authenticationProvider;
    private final AgentNameClient agentNameClient;

    // Regex pattern for validating email addresses
    private final Pattern emailPattern = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");

    public UserService(UserRepository userRepository,
                       AgentNameClient agentNameClient,
                       PasswordEncoder passwordEncoder,
                       CustomAuthenticationProvider authenticationProvider,
                       AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.agentNameClient = agentNameClient;
        this.passwordEncoder = passwordEncoder;
        this.authenticationProvider = authenticationProvider;
        this.authenticationManager = authenticationManager;
    }

    /**
     * Creates a new user in the system.
     * @param user The user to be created.
     * @return The created user as a UserDTO.
     * @throws UserAlreadyExistsException if the user already exists.
     */
    public UserResponse createUser(User user) {
        Map<String, String> errors = validateUser(user);
        if (!errors.isEmpty()) {
            throw new UserAlreadyExistsException("User already exists with provided details", errors);
        }

        sanitizeUser(user);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        User savedUser = userRepository.save(user);
        return convertToDTO(savedUser);
    }

    /**
     * Authenticates a user with the provided authentication request.
     * @param authenticationRequest The authentication request containing username and password.
     * @return The authenticated user.
     * @throws UsernameNotFoundException if the user is not found.
     */
    public User authenticate(AuthenticationRequest authenticationRequest) {
        String username = authenticationRequest.getUsername();
//        authenticationManager.authenticate(
//                new UsernamePasswordAuthenticationToken(username, authenticationRequest.getPassword()));
        authenticationProvider.authenticate(new UsernamePasswordAuthenticationToken(username, authenticationRequest.getPassword()));

        return userRepository.findByUsername(username)
                .or(() -> userRepository.findByEmail(username))
                .or(() -> userRepository.findByPhone(username))
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

    }

    /**
     * Retrieves a user by their ID.
     * @param id The ID of the user.
     * @return The user as a UserDTO.
     * @throws ResourceNotFoundException if the user with the specified ID is not found.
     */
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User with ID " + id + " not found"));
        return convertToDTO(user);
    }

    /**
     * Retrieves all users in the system.
     * @return A list of all users as UserDTOs.
     */
    public List<UserResponse> getAllUsers() {
        List<User> users = userRepository.findAll();
        List<UserResponse> userResponses = new ArrayList<>();
        users.forEach(user -> userResponses.add(convertToDTO(user)));
        return userResponses;
    }

    /**
     * Retrieves a user by their email.
     * @param email The email of the user.
     * @return The user as a UserDTO.
     * @throws ResourceNotFoundException if the user with the specified email is not found.
     */
    public UserResponse getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User with email " + email + " not found"));
        return convertToDTO(user);
    }

    public UserResponse getUserByPhone(String phoneNumber) {
        User user = userRepository.findByPhone(phoneNumber)
                .orElseThrow(() -> new ResourceNotFoundException("User with phone " + phoneNumber + " not found"));
        return convertToDTO(user);
    }

    public UserResponse getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User with username " + username + " not found"));
        return convertToDTO(user);
    }

    // Additional methods for retrieving users by username, phone, etc. can follow a similar pattern

    /**
     * Changes the username of the user with the specified ID.
     * @param id The ID of the user whose username is to be changed.
     * @return The updated user as a UserDTO.
     */
    public UserResponse changeUsername(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User with ID " + id + " not found"));
        user.setUsername(generateUsername(user.getUsername()).toLowerCase(Locale.ROOT));
        userRepository.save(user);
        return convertToDTO(user);
    }

    // Username generation, validation, and other helper methods...

    private void sanitizeUser(User user) {
        user.setEmail(user.getEmail().toLowerCase(Locale.ROOT));
        user.setFirstName(user.getFirstName().toLowerCase(Locale.ROOT));
        user.setLastName(user.getLastName().toLowerCase(Locale.ROOT));
        user.setUsername(generateUsername(user.getEmail()));
    }

    private String generateUsername(String fallbackUsername) {
        try {
            String username;
            do {
                AgentName agentName = agentNameClient.generateAgentName();
                username = agentName.getName().toLowerCase(Locale.ROOT);
            } while (userRepository.findByUsername(username).isPresent());
            return username;
        } catch (Exception e) {
            // Log error and fallback to using email as username
            return fallbackUsername.toLowerCase(Locale.ROOT);
        }
    }

    private Map<String, String> validateUser(User user) {
        Map<String, String> errors = new HashMap<>();
        validateEmail(user.getEmail(), errors);
        validateField("firstName", user.getFirstName(), errors);
        validateField("lastName", user.getLastName(), errors);
        validatePhone(user.getPhone(), errors);
        validatePassword(user.getPassword(), errors);
        return errors;
    }

    private void validateEmail(String email, Map<String, String> errors) {
        if (email == null || email.isEmpty()) {
            errors.put("email", "Email is required");
        } else if (!isEmailValid(email)) {
            errors.put("email", "Invalid email format");
        } else if (userRepository.findByEmail(email).isPresent()) {
            errors.put("email", "Email is already in use. Please try a different email");
        }
    }

    private void validateField(String fieldName, String value, Map<String, String> errors) {
        if (value == null || value.isEmpty()) {
            errors.put(fieldName, fieldName + " is required");
        }
    }

    private void validatePhone(String phone, Map<String, String> errors) {
        if (phone == null || phone.isEmpty()) {
            errors.put("phone", "Phone number is required");
        } else if (!phone.matches("^[+][0-9]{10,13}$")) {
            errors.put("phone", "Enter a valid phone number with country code (e.g., +919876543210)");
        } else if (userRepository.findByPhone(phone).isPresent()) {
            errors.put("phone", "Phone number is already registered. Please try a different phone number");
        }
    }

    private void validatePassword(String password, Map<String, String> errors) {
        if (password == null || password.isEmpty()) {
            errors.put("password", "Password is required");
        } else if (!isPasswordValid(password)) {
            errors.put("password", "Password must be 8-16 characters long, contain at least one digit, one uppercase letter, and one special character");
        }
    }

    private boolean isEmailValid(String email) {
        return emailPattern.matcher(email).matches();
    }

    private boolean isPasswordValid(String password) {
        return (
                password.length() >= 8 &&
                        password.length() <= 16 &&
                        password.matches(".*\\d.*") && // At least one digit
                        password.matches(".*[A-Z].*") && // At least one uppercase letter
                        password.matches(".*[._!@#$%^&*()?,].*") // At least one special character
        );
    }


    private UserResponse convertToDTO(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getPhone(),
                user.getUsername());
    }


}
