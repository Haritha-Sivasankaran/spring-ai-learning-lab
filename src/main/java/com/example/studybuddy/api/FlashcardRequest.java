package com.example.studybuddy.api;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import jakarta.validation.constraints.NotNull;

public record FlashcardRequest(
        @NotBlank(message = "Topic is required")
        @Size(min = 2, max = 160, message = "Topic must be between 2 and 160 characters")
        String topic,

        @NotBlank(message = "Difficulty level is required")
        @Size(min = 2, max = 40, message = "Level must be between 2 and 40 characters")
        String level,

        @NotNull(message = "Count is required")
        @Min(value = 1, message = "Must request at least 1 card")
        @Max(value = 12, message = "Cannot request more than 12 cards")
        Integer count) {
}
