package com.example.studybuddy.api;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.List;

@JsonPropertyOrder({"provider", "answer", "suggestedNextSteps"})
public record ExplainResponse(
        String provider,
        String answer,
        List<String> suggestedNextSteps) {
}
