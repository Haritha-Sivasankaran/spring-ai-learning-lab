package com.example.studybuddy.security;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class MdcFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        try {
            String requestId = UUID.randomUUID().toString();
            MDC.put("requestId", requestId);

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String userId = (auth != null && auth.isAuthenticated()) ? auth.getName() : "anonymous";
            MDC.put("userId", userId);

            chain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }
}
