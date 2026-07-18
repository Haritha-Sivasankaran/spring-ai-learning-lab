package com.example.studybuddy.config;

import com.example.studybuddy.security.QuotaInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final QuotaInterceptor quotaInterceptor;

    public WebConfig(QuotaInterceptor quotaInterceptor) {
        this.quotaInterceptor = quotaInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(quotaInterceptor)
                .addPathPatterns("/api/study/explain", "/api/study/quiz", "/api/study/flashcards");
    }
}
