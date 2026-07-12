package com.example.studybuddy.ai;

import com.example.studybuddy.api.ExplainRequest;
import com.example.studybuddy.api.ExplainResponse;
import com.example.studybuddy.api.FlashcardDeckResponse;
import com.example.studybuddy.api.FlashcardRequest;
import com.example.studybuddy.api.QuizRequest;
import com.example.studybuddy.api.QuizResponse;
import java.util.List;
import java.util.Objects;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("openai")
public class SpringAiStudyClient implements StudyAiClient {

    private static final String PROVIDER = "spring-ai-openai";

    private final ChatClient chatClient;

    public SpringAiStudyClient(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder
                .defaultSystem("""
                        You are StudyBuddy, a patient AI tutor for software engineering students.
                        Prefer concise explanations, practical examples, and active recall.
                        Do not invent citations. If the student asks about a version-sensitive topic,
                        tell them to verify against the official documentation.
                        """)
                .build();
    }

    @Override
    public String provider() {
        return PROVIDER;
    }

    @Override
    public ExplainResponse explain(ExplainRequest request) {
        String answer = this.chatClient.prompt()
                .user(user -> user.text("""
                        Student level: {level}
                        Learning goal: {goal}

                        Question:
                        {question}

                        Explain the answer in this format:
                        - Plain-English idea
                        - A small Java or Spring example when useful
                        - Common mistake to avoid
                        - Three recap bullets
                        - One practice task
                        """)
                        .param("level", valueOr(request.level(), "beginner"))
                        .param("goal", valueOr(request.goal(), "understand the concept well enough to explain it"))
                        .param("question", request.question()))
                .call()
                .content();

        return new ExplainResponse(
                PROVIDER,
                Objects.requireNonNullElse(answer, "No response was returned by the model."),
                List.of("Change the level and ask again", "Turn the answer into flashcards", "Generate a quiz"));
    }

    @Override
    public QuizResponse quiz(QuizRequest request) {
        int count = clamp(request.numberOfQuestions(), 3, 1, 10);
        QuizResponse response = this.chatClient.prompt()
                .user(user -> user.text("""
                        Create a learning quiz about {topic}.
                        Difficulty: {difficulty}
                        Number of questions: {count}

                        Rules:
                        - Return exactly {count} questions.
                        - Each question must have exactly four options.
                        - The answer must exactly match one option.
                        - Keep explanations short and useful.
                        """)
                        .param("topic", request.topic())
                        .param("difficulty", valueOr(request.difficulty(), "beginner"))
                        .param("count", count))
                .call()
                .entity(QuizResponse.class);

        return withProvider(response);
    }

    @Override
    public FlashcardDeckResponse flashcards(FlashcardRequest request) {
        int count = clamp(request.count(), 5, 1, 12);
        FlashcardDeckResponse response = this.chatClient.prompt()
                .user(user -> user.text("""
                        Create flashcards for the topic: {topic}
                        Student level: {level}
                        Count: {count}

                        Rules:
                        - Return exactly {count} flashcards.
                        - Front should ask one focused question.
                        - Back should answer in two or three sentences.
                        - Hint should be short and not reveal the full answer.
                        """)
                        .param("topic", request.topic())
                        .param("level", valueOr(request.level(), "beginner"))
                        .param("count", count))
                .call()
                .entity(FlashcardDeckResponse.class);

        return withProvider(response);
    }

    private static QuizResponse withProvider(QuizResponse response) {
        if (response == null) {
            return new QuizResponse(PROVIDER, "Quiz", "unknown", List.of());
        }
        return new QuizResponse(PROVIDER, response.title(), response.difficulty(), response.questions());
    }

    private static FlashcardDeckResponse withProvider(FlashcardDeckResponse response) {
        if (response == null) {
            return new FlashcardDeckResponse(PROVIDER, "Flashcards", "unknown", List.of());
        }
        return new FlashcardDeckResponse(PROVIDER, response.topic(), response.level(), response.cards());
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
