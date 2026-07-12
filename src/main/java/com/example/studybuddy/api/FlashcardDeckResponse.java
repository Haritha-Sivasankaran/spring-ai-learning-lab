package com.example.studybuddy.api;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.List;

@JsonPropertyOrder({"provider", "topic", "level", "cards"})
public record FlashcardDeckResponse(
        String provider,
        String topic,
        String level,
        List<Flashcard> cards) {
}
