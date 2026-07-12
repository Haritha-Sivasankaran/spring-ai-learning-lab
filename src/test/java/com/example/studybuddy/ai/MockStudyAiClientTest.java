package com.example.studybuddy.ai;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.studybuddy.api.ExplainRequest;
import com.example.studybuddy.api.FlashcardRequest;
import com.example.studybuddy.api.QuizRequest;
import org.junit.jupiter.api.Test;

class MockStudyAiClientTest {

    private final MockStudyAiClient client = new MockStudyAiClient();

    @Test
    void explainsWithoutCallingARealModel() {
        var response = this.client.explain(new ExplainRequest(
                "What is Spring AI ChatClient?",
                "beginner",
                "prepare for a demo"));

        assertThat(response.provider()).isEqualTo("mock");
        assertThat(response.answer()).contains("ChatClient");
        assertThat(response.suggestedNextSteps()).isNotEmpty();
    }

    @Test
    void createsRequestedNumberOfQuizQuestions() {
        var response = this.client.quiz(new QuizRequest(
                "Spring AI structured output",
                "beginner",
                4));

        assertThat(response.provider()).isEqualTo("mock");
        assertThat(response.questions()).hasSize(4);
        assertThat(response.questions().get(0).options()).contains("ChatClient");
    }

    @Test
    void createsRequestedNumberOfFlashcards() {
        var response = this.client.flashcards(new FlashcardRequest(
                "Spring profiles",
                "beginner",
                6));

        assertThat(response.provider()).isEqualTo("mock");
        assertThat(response.cards()).hasSize(6);
        assertThat(response.cards().get(0).front()).contains("Spring profiles");
    }
}
