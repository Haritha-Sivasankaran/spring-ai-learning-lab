package com.example.studybuddy.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ExplainRequest(
        @NotBlank
        @Size(max = 2000)
        String question,

        @Size(max = 80)
        String level,

        @Size(max = 240)
        String goal) {
}
