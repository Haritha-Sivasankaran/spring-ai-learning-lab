package com.example.studybuddy.dto;

import java.util.Map;

public record StudyProgressResponse(
        int explains,
        int quizzes,
        int flashcards,
        Map<String, Object> moduleProgress
) {
}
