package com.example.studybuddy.config;

import com.example.studybuddy.security.JwtAuthenticationFilter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${app.cors.allowed-origins}")
    private String allowedOrigins;

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final Environment environment;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter, Environment environment) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.environment = environment;
    }

    @Bean
    public static PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        boolean isOpenAiActive = Arrays.asList(environment.getActiveProfiles()).contains("openai");

        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable()) // Disable CSRF for simplified REST operations
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.disable())) // Enable H2 console frames
            .authorizeHttpRequests(auth -> {
                // Public authentication and metadata endpoints
                auth.requestMatchers(
                    "/",
                    "/index.html",
                    "/static/**",
                    "/css/**",
                    "/js/**",
                    "/assets/**",
                    "/favicon.ico",
                    "/api/study/status",
                    "/api/auth/login",
                    "/api/auth/register",
                    "/h2-console/**"
                ).permitAll();

                // Dynamic study endpoint authorization
                if (isOpenAiActive) {
                    // Under openai profile, require token authentication for all study logic
                    auth.requestMatchers("/api/study/**").authenticated();
                } else {
                    // Under mock profile, allow local development/testing without credentials
                    auth.requestMatchers("/api/study/**").permitAll();
                }

                auth.anyRequest().authenticated();
            })
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        
        // Parse dynamic origins list
        List<String> origins = Arrays.stream(allowedOrigins.split(","))
                                     .map(String::trim)
                                     .toList();
        config.setAllowedOrigins(origins);
        
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        config.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "Cache-Control", "Accept", "X-Requested-With"));
        config.setAllowCredentials(true);
        config.setExposedHeaders(Collections.singletonList("Set-Cookie"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
