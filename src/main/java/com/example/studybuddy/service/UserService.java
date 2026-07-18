package com.example.studybuddy.service;

import com.example.studybuddy.dto.RegisterRequest;
import com.example.studybuddy.dto.UserResponse;
import com.example.studybuddy.model.User;
import com.example.studybuddy.repository.UserRepository;
import java.util.Collections;
import java.util.UUID;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        // Pre-populate a test user: test@example.com / password if database is empty
        if (this.userRepository.findByEmail("test@example.com").isEmpty()) {
            register(new RegisterRequest("test@example.com", "password", "Test Student"));
        }
    }

    public UserResponse register(RegisterRequest request) {
        if (userRepository.findByEmail(request.email().toLowerCase()).isPresent()) {
            throw new IllegalArgumentException("User with this email already exists");
        }

        String userId = UUID.randomUUID().toString();
        String hashedPassword = passwordEncoder.encode(request.password());
        User user = new User(userId, request.email().toLowerCase(), hashedPassword, request.name());
        userRepository.save(user);

        return new UserResponse(user.getId(), user.getEmail(), user.getName(), null);
    }

    public UserResponse getUserByEmail(String email) {
        User user = userRepository.findByEmail(email.toLowerCase())
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
        return new UserResponse(user.getId(), user.getEmail(), user.getName(), null);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(username.toLowerCase())
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + username));
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                Collections.singletonList(() -> "ROLE_USER")
        );
    }
}
