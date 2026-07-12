package com.example.studybuddy.api;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"front", "back", "hint"})
public record Flashcard(
        String front,
        String back,
        String hint) {
}
