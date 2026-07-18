package com.example.studybuddy.ai;

import com.example.studybuddy.api.ExplainRequest;
import com.example.studybuddy.api.ExplainResponse;
import com.example.studybuddy.api.FlashcardDeckResponse;
import com.example.studybuddy.api.FlashcardRequest;
import com.example.studybuddy.api.QuizRequest;
import com.example.studybuddy.api.QuizResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Objects;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("openai")
public class SpringAiStudyClient implements StudyAiClient {

    private static final String PROVIDER = "spring-ai-openai";
    private static final Logger log = LoggerFactory.getLogger(SpringAiStudyClient.class);

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;

    public SpringAiStudyClient(ChatClient.Builder chatClientBuilder, ObjectMapper objectMapper) {
        this.chatClient = chatClientBuilder
                .defaultSystem("""
                        You are StudyBuddy, a patient AI tutor for software engineering students.
                        Prefer concise explanations, practical examples, and active recall.
                        Do not invent citations. If the student asks about a version-sensitive topic,
                        tell them to verify against the official documentation.
                        """)
                .build();
        this.objectMapper = objectMapper;
    }

    @Override
    public String provider() {
        return PROVIDER;
    }

    @Override
    @CircuitBreaker(name = "studyClient", fallbackMethod = "explainFallback")
    @Retry(name = "studyClient")
    public ExplainResponse explain(ExplainRequest request) {
        long startTime = System.currentTimeMillis();
        ChatResponse chatResponse = this.chatClient.prompt()
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
                .chatResponse();

        long latency = System.currentTimeMillis() - startTime;
        String answer = chatResponse.getResult().getOutput().getText();
        logAiCall("/api/study/explain", latency, chatResponse.getMetadata().getUsage());

        return new ExplainResponse(
                PROVIDER,
                Objects.requireNonNullElse(answer, "No response was returned by the model."),
                List.of("Change the level and ask again", "Turn the answer into flashcards", "Generate a quiz"));
    }

    @Override
    @CircuitBreaker(name = "studyClient", fallbackMethod = "quizFallback")
    @Retry(name = "studyClient")
    public QuizResponse quiz(QuizRequest request) {
        int count = clamp(request.numberOfQuestions(), 3, 1, 10);
        long startTime = System.currentTimeMillis();
        ChatResponse chatResponse = this.chatClient.prompt()
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
                .chatResponse();

        long latency = System.currentTimeMillis() - startTime;
        String rawContent = chatResponse.getResult().getOutput().getText();
        String cleanedContent = cleanJsonContent(rawContent);

        QuizResponse response;
        try {
            response = objectMapper.readValue(cleanedContent, QuizResponse.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse quiz response JSON: " + cleanedContent, e);
        }

        logAiCall("/api/study/quiz", latency, chatResponse.getMetadata().getUsage());
        return withProvider(response);
    }

    @Override
    @CircuitBreaker(name = "studyClient", fallbackMethod = "flashcardsFallback")
    @Retry(name = "studyClient")
    public FlashcardDeckResponse flashcards(FlashcardRequest request) {
        int count = clamp(request.count(), 5, 1, 12);
        long startTime = System.currentTimeMillis();
        ChatResponse chatResponse = this.chatClient.prompt()
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
                .chatResponse();

        long latency = System.currentTimeMillis() - startTime;
        String rawContent = chatResponse.getResult().getOutput().getContent();
        String cleanedContent = cleanJsonContent(rawContent);

        FlashcardDeckResponse response;
        try {
            response = objectMapper.readValue(cleanedContent, FlashcardDeckResponse.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse flashcard response JSON: " + cleanedContent, e);
        }

        logAiCall("/api/study/flashcards", latency, chatResponse.getMetadata().getUsage());
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

    private String cleanJsonContent(String content) {
        if (content == null) {
            return "";
        }
        content = content.trim();
        if (content.startsWith("```json")) {
            content = content.substring(7);
        } else if (content.startsWith("```")) {
            content = content.substring(3);
        }
        if (content.endsWith("```")) {
            content = content.substring(0, content.length() - 3);
        }
        return content.trim();
    }

    private void logAiCall(String endpoint, long latency, Usage usage) {
        long promptTokens = usage != null ? usage.getPromptTokens() : 0;
        long generationTokens = usage != null ? usage.getGenerationTokens() : 0;
        long totalTokens = usage != null ? usage.getTotalTokens() : 0;

        String requestId = Objects.requireNonNullElse(MDC.get("requestId"), "N/A");
        String userId = Objects.requireNonNullElse(MDC.get("userId"), "anonymous");

        log.info("[AI_CALL] requestId=\"{}\" userId=\"{}\" endpoint=\"{}\" latencyMs={} promptTokens={} generationTokens={} totalTokens={}",
                requestId, userId, endpoint, latency, promptTokens, generationTokens, totalTokens);
    }

    // Fallback methods for Resilience4j
    public ExplainResponse explainFallback(ExplainRequest request, Throwable t) {
        return new ExplainResponse(
                PROVIDER + "-fallback",
                "The AI tutor is currently unavailable. Please try again in a moment. (Error: " + t.getMessage() + ")",
                List.of("Try again later")
        );
    }

    public QuizResponse quizFallback(QuizRequest request, Throwable t) {
        return new QuizResponse(
                PROVIDER + "-fallback",
                "Quiz Unavailable",
                valueOr(request.difficulty(), "beginner"),
                List.of()
        );
    }

    public FlashcardDeckResponse flashcardsFallback(FlashcardRequest request, Throwable t) {
        return new FlashcardDeckResponse(
                PROVIDER + "-fallback",
                request.topic(),
                valueOr(request.level(), "beginner"),
                List.of()
        );
    }
}
