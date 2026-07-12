package com.example.studybuddy.ai;

import com.example.studybuddy.api.ExplainRequest;
import com.example.studybuddy.api.ExplainResponse;
import com.example.studybuddy.api.Flashcard;
import com.example.studybuddy.api.FlashcardDeckResponse;
import com.example.studybuddy.api.FlashcardRequest;
import com.example.studybuddy.api.QuizQuestion;
import com.example.studybuddy.api.QuizRequest;
import com.example.studybuddy.api.QuizResponse;
import java.util.List;
import java.util.stream.IntStream;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("!openai")
public class MockStudyAiClient implements StudyAiClient {

    private static final String PROVIDER = "mock";

    @Override
    public String provider() {
        return PROVIDER;
    }

    @Override
    public ExplainResponse explain(ExplainRequest request) {
        String level = valueOr(request.level(), "beginner");
        String goal = valueOr(request.goal(), "understand the idea");
        String answer = """
                Mock mode answer for a %s learner.

                Question: %s

                Plain-English idea:
                Spring AI gives Spring Boot applications a clean abstraction over AI models. The main class you will use in this project is ChatClient, which lets your service build a prompt, pass user parameters, call a model, and return either text or a Java object.

                Small example:
                chatClient.prompt()
                    .user(user -> user.text("Explain {topic}").param("topic", "ChatClient"))
                    .call()
                    .content();

                Goal fit:
                Use this answer to %s.

                Recap:
                - Keep prompts close to the business method that needs them.
                - Use structured output when your controller needs JSON-like objects.
                - Use the openai profile only after setting OPENAI_API_KEY.

                Practice task:
                Change the quiz prompt in SpringAiStudyClient and observe how the response shape changes.
                """.formatted(level, request.question(), goal);

        return new ExplainResponse(
                PROVIDER,
                answer,
                List.of("Open SpringAiStudyClient", "Run a quiz request", "Switch to the openai profile"));
    }

    @Override
    public QuizResponse quiz(QuizRequest request) {
        int count = clamp(request.numberOfQuestions(), 3, 1, 10);
        String difficulty = valueOr(request.difficulty(), "beginner");
        List<QuizQuestion> questions = IntStream.rangeClosed(1, count)
                .mapToObj(index -> new QuizQuestion(
                        "Mock question %d: What is one useful Spring AI idea for %s?".formatted(index, request.topic()),
                        List.of("ChatClient", "EntityManager", "JSP scriptlets", "Servlet filters only"),
                        "ChatClient",
                        "ChatClient is the fluent API this sample uses to send prompts and receive responses."))
                .toList();

        return new QuizResponse(PROVIDER, "Mock quiz: " + request.topic(), difficulty, questions);
    }

    @Override
    public FlashcardDeckResponse flashcards(FlashcardRequest request) {
        int count = clamp(request.count(), 5, 1, 12);
        String level = valueOr(request.level(), "beginner");
        List<Flashcard> cards = IntStream.rangeClosed(1, count)
                .mapToObj(index -> new Flashcard(
                        "Mock card %d: What should I notice about %s?".formatted(index, request.topic()),
                        "In the real openai profile, this card would come from a model. In mock mode, it is deterministic so you can learn the API without spending tokens.",
                        "Look at profiles and the StudyAiClient interface."))
                .toList();

        return new FlashcardDeckResponse(PROVIDER, request.topic(), level, cards);
    }

    private static String valueOr(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return value.trim();
    }

    private static int clamp(Integer value, int fallback, int min, int max) {
        int candidate = value == null ? fallback : value;
        return Math.max(min, Math.min(max, candidate));
    }
}
