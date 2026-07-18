package com.example.studybuddy.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ExplainRequest(
        @NotBlank(message = "Question is required")
        @Size(min = 5, max = 2000, message = "Question must be between 5 and 2000 characters")
        String question,

        @NotBlank(message = "Difficulty level is required")
        @Size(min = 2, max = 80, message = "Level must be between 2 and 80 characters")
        String level,

        @NotBlank(message = "Learning goal is required")
        @Size(min = 2, max = 240, message = "Goal must be between 2 and 240 characters")
        String goal) {
}
