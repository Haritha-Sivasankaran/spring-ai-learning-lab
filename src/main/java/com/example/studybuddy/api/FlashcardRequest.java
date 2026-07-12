package com.example.studybuddy.api;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record FlashcardRequest(
        @NotBlank
        @Size(max = 160)
        String topic,

        @Size(max = 40)
        String level,

        @Min(1)
        @Max(12)
        Integer count) {
}
