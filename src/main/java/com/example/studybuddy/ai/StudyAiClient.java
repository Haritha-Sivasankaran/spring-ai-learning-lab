package com.example.studybuddy.ai;

import com.example.studybuddy.api.ExplainRequest;
import com.example.studybuddy.api.ExplainResponse;
import com.example.studybuddy.api.FlashcardDeckResponse;
import com.example.studybuddy.api.FlashcardRequest;
import com.example.studybuddy.api.QuizRequest;
import com.example.studybuddy.api.QuizResponse;

public interface StudyAiClient {

    String provider();

    ExplainResponse explain(ExplainRequest request);

    QuizResponse quiz(QuizRequest request);

    FlashcardDeckResponse flashcards(FlashcardRequest request);
}
