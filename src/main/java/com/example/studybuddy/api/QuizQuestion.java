package com.example.studybuddy.api;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.List;

@JsonPropertyOrder({"question", "options", "answer", "explanation"})
public record QuizQuestion(
        String question,
        List<String> options,
        String answer,
        String explanation) {
}
