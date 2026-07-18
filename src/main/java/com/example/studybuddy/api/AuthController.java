package com.example.studybuddy.api;

import com.example.studybuddy.dto.LoginRequest;
import com.example.studybuddy.dto.RegisterRequest;
import com.example.studybuddy.dto.UserResponse;
import com.example.studybuddy.security.JwtTokenProvider;
import com.example.studybuddy.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    public AuthController(UserService userService, AuthenticationManager authenticationManager, JwtTokenProvider tokenProvider) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        UserResponse response = userService.register(request);
        // Generate token for automatic login upon registration
        String token = tokenProvider.generateToken(response.email());
        UserResponse authenticatedResponse = new UserResponse(response.id(), response.email(), response.name(), token);
        return ResponseEntity.status(HttpStatus.CREATED).body(authenticatedResponse);
    }

    @PostMapping("/login")
    public ResponseEntity<UserResponse> login(@Valid @RequestBody LoginRequest request) {
        UsernamePasswordAuthenticationToken authRequest = 
            new UsernamePasswordAuthenticationToken(request.email().toLowerCase(), request.password());
        
        Authentication authentication = authenticationManager.authenticate(authRequest);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        String token = tokenProvider.generateToken(request.email().toLowerCase());
        UserResponse response = userService.getUserByEmail(request.email());
        UserResponse authenticatedResponse = new UserResponse(response.id(), response.email(), response.name(), token);
        
        return ResponseEntity.ok(authenticatedResponse);
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> me() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        UserResponse response = userService.getUserByEmail(auth.getName());
        return ResponseEntity.ok(response);
    }
}
