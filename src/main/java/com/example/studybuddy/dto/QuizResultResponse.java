package com.example.studybuddy.dto;

import java.time.LocalDateTime;

public record QuizResultResponse(
        String id,
        String topic,
        int score,
        int totalQuestions,
        LocalDateTime completedAt
) {
}
