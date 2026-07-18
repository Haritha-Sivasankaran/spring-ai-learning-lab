package com.example.studybuddy.service;

import com.example.studybuddy.dto.RegisterRequest;
import com.example.studybuddy.dto.UserResponse;
import com.example.studybuddy.model.User;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService implements UserDetailsService {

    private final Map<String, User> userMap = new ConcurrentHashMap<>();
    private final PasswordEncoder passwordEncoder;

    public UserService(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
        // Pre-populate a test user: test@example.com / password
        register(new RegisterRequest("test@example.com", "password", "Test Student"));
    }

    public UserResponse register(RegisterRequest request) {
        if (userMap.containsKey(request.email().toLowerCase())) {
            throw new IllegalArgumentException("User with this email already exists");
        }

        String userId = UUID.randomUUID().toString();
        String hashedPassword = passwordEncoder.encode(request.password());
        User user = new User(userId, request.email().toLowerCase(), hashedPassword, request.name());
        userMap.put(user.getEmail(), user);

        return new UserResponse(user.getId(), user.getEmail(), user.getName());
    }

    public UserResponse getUserByEmail(String email) {
        User user = userMap.get(email.toLowerCase());
        if (user == null) {
            throw new UsernameNotFoundException("User not found: " + email);
        }
        return new UserResponse(user.getId(), user.getEmail(), user.getName());
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userMap.get(username.toLowerCase());
        if (user == null) {
            throw new UsernameNotFoundException("User not found with email: " + username);
        }
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                Collections.singletonList(() -> "ROLE_USER")
        );
    }
}
