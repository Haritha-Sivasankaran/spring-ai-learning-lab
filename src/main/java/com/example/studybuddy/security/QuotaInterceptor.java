package com.example.studybuddy.security;

import com.example.studybuddy.model.User;
import com.example.studybuddy.repository.UserRepository;
import com.example.studybuddy.service.QuotaService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;

@Component
public class QuotaInterceptor implements HandlerInterceptor {

    private final QuotaService quotaService;
    private final UserRepository userRepository;
    
    @Value("${app.quota.daily-limit:10}")
    private int dailyLimit;

    public QuotaInterceptor(QuotaService quotaService, UserRepository userRepository) {
        this.quotaService = quotaService;
        this.userRepository = userRepository;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
            String email = authentication.getName();
            Optional<User> userOpt = userRepository.findByEmail(email.toLowerCase());
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                boolean allowed = quotaService.incrementAndCheckQuota(user, dailyLimit);
                if (!allowed) {
                    response.setStatus(429);
                    response.setContentType("application/json");
                    response.setCharacterEncoding("UTF-8");
                    String jsonResponse = String.format(
                            "{\"status\":429,\"error\":\"Too Many Requests\",\"message\":\"You have exceeded your daily quota of %d AI calls. Please try again tomorrow.\",\"timestamp\":\"%s\"}",
                            dailyLimit, LocalDateTime.now()
                    );
                    response.getWriter().write(jsonResponse);
                    return false; // Block request
                }
            }
        }
        return true;
    }
}
