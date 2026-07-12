package com.example.studybuddy.api;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.List;

@JsonPropertyOrder({"provider", "title", "difficulty", "questions"})
public record QuizResponse(
        String provider,
        String title,
        String difficulty,
        List<QuizQuestion> questions) {
}
