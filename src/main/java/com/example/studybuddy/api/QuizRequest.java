package com.example.studybuddy.api;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import jakarta.validation.constraints.NotNull;

public record QuizRequest(
        @NotBlank(message = "Topic is required")
        @Size(min = 2, max = 160, message = "Topic must be between 2 and 160 characters")
        String topic,

        @NotBlank(message = "Difficulty is required")
        @Size(min = 2, max = 40, message = "Difficulty must be between 2 and 40 characters")
        String difficulty,

        @NotNull(message = "Number of questions is required")
        @Min(value = 1, message = "Must request at least 1 question")
        @Max(value = 10, message = "Cannot request more than 10 questions")
        Integer numberOfQuestions) {
}
