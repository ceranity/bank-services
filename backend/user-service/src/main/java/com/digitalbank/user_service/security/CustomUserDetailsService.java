package com.digitalbank.user_service.security;

import com.digitalbank.user_service.models.User;
import com.digitalbank.user_service.repositories.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    @Override
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        // Try to find the user by username, email, or phone
        User user = userRepository.findByUsername(identifier)
                .or(() -> userRepository.findByEmail(identifier))
                .or(() -> userRepository.findByPhone(identifier))
                .orElseThrow(() -> new UsernameNotFoundException(identifier));
        System.out.println("User: " + user);
//        User user = userRepository.findByUsername(identifier)
//                .orElse(userRepository.findByEmail(identifier)
//                            .orElse(userRepository.findByPhone(identifier)
//                                .orElseThrow(() -> new UsernameNotFoundException("User not found with identifier: " + identifier))));
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                new ArrayList<>()); // Customize as needed with roles or authorities
    }


}
