package com.example.studybuddy.api;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record QuizRequest(
        @NotBlank
        @Size(max = 160)
        String topic,

        @Size(max = 40)
        String difficulty,

        @Min(1)
        @Max(10)
        Integer numberOfQuestions) {
}
