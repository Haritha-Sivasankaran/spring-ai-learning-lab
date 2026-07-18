package com.example.studybuddy.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record QuizResultRequest(
        @NotBlank String topic,
        @Min(0) int score,
        @Min(1) int totalQuestions
) {
}
