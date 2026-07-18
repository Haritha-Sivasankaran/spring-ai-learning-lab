package com.example.studybuddy.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.Map;

public record StudyProgressRequest(
        @Min(0) int explains,
        @Min(0) int quizzes,
        @Min(0) int flashcards,
        @NotNull Map<String, Object> moduleProgress
) {
}
