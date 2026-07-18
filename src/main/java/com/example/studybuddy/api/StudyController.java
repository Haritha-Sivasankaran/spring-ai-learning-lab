package com.example.studybuddy.api;

import com.example.studybuddy.ai.StudyAiClient;
import com.example.studybuddy.dto.QuizResultRequest;
import com.example.studybuddy.dto.QuizResultResponse;
import com.example.studybuddy.dto.StudyProgressRequest;
import com.example.studybuddy.dto.StudyProgressResponse;
import com.example.studybuddy.model.FlashcardDeck;
import com.example.studybuddy.model.QuizResult;
import com.example.studybuddy.model.StudyProgress;
import com.example.studybuddy.model.User;
import com.example.studybuddy.repository.FlashcardDeckRepository;
import com.example.studybuddy.repository.QuizResultRepository;
import com.example.studybuddy.repository.StudyProgressRepository;
import com.example.studybuddy.repository.UserRepository;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/study")
public class StudyController {

    private final StudyAiClient studyAiClient;
    private final Environment environment;
    private final UserRepository userRepository;
    private final StudyProgressRepository studyProgressRepository;
    private final QuizResultRepository quizResultRepository;
    private final FlashcardDeckRepository flashcardDeckRepository;
    private final ObjectMapper objectMapper;

    public StudyController(StudyAiClient studyAiClient,
                           Environment environment,
                           UserRepository userRepository,
                           StudyProgressRepository studyProgressRepository,
                           QuizResultRepository quizResultRepository,
                           FlashcardDeckRepository flashcardDeckRepository,
                           ObjectMapper objectMapper) {
        this.studyAiClient = studyAiClient;
        this.environment = environment;
        this.userRepository = userRepository;
        this.studyProgressRepository = studyProgressRepository;
        this.quizResultRepository = quizResultRepository;
        this.flashcardDeckRepository = flashcardDeckRepository;
        this.objectMapper = objectMapper;
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User is not authenticated");
        }
        String email = authentication.getName();
        return userRepository.findByEmail(email.toLowerCase())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    }

    @GetMapping("/status")
    public Map<String, Object> status() {
        return Map.of(
                "app", "spring-ai-learning-lab",
                "provider", this.studyAiClient.provider(),
                "profiles", profiles());
    }

    @GetMapping("/verify/{challengeId}")
    public Map<String, Object> verifyChallenge(@PathVariable String challengeId) {
        boolean passed = false;
        String message = "";
        String details = "";

        try {
            switch (challengeId) {
                case "challenge-1": {
                    java.nio.file.Path pathMock = java.nio.file.Paths.get("src/main/java/com/example/studybuddy/ai/MockStudyAiClient.java");
                    java.nio.file.Path pathReal = java.nio.file.Paths.get("src/main/java/com/example/studybuddy/ai/SpringAiStudyClient.java");
                    
                    boolean mockChanged = false;
                    if (java.nio.file.Files.exists(pathMock)) {
                        String content = java.nio.file.Files.readString(pathMock);
                        if (!content.contains("Mock mode answer for a %s learner.") || content.contains("StudyBuddy Pro") || content.contains("JavaGuru")) {
                            mockChanged = true;
                        }
                    }
                    
                    boolean realChanged = false;
                    if (java.nio.file.Files.exists(pathReal)) {
                        String content = java.nio.file.Files.readString(pathReal);
                        if (!content.contains("You are StudyBuddy, a patient AI tutor for software engineering students.") || content.contains("StudyBuddy Pro") || content.contains("JavaGuru")) {
                            realChanged = true;
                        }
                    }

                    if (mockChanged || realChanged) {
                        passed = true;
                        message = "Challenge 1 Passed! Prompt customization detected.";
                        details = "We detected changes in your AI Client prompt configuration files. Great job customizing the Tutor's persona!";
                    } else {
                        passed = false;
                        message = "Challenge 1 Failed: Prompt customization not detected.";
                        details = "Modify either 'MockStudyAiClient.java' (the answer string) or 'SpringAiStudyClient.java' (the system prompt string) to personalize the tutor's greeting, then run this test again.";
                    }
                    break;
                }
                case "challenge-2": {
                    boolean foundMapping = false;
                    for (java.lang.reflect.Method method : StudyController.class.getDeclaredMethods()) {
                        PostMapping postMapping = method.getAnnotation(PostMapping.class);
                        if (postMapping != null && java.util.Arrays.asList(postMapping.value()).contains("/lesson-plan")) {
                            foundMapping = true;
                        }
                        RequestMapping requestMapping = method.getAnnotation(RequestMapping.class);
                        if (requestMapping != null && java.util.Arrays.asList(requestMapping.value()).contains("/lesson-plan")) {
                            foundMapping = true;
                        }
                    }

                    if (foundMapping) {
                        passed = true;
                        message = "Challenge 2 Passed! Mapped endpoint '/api/study/lesson-plan' detected.";
                        details = "Reflection checks successfully found the mapping in StudyController.java!";
                    } else {
                        passed = false;
                        message = "Challenge 2 Failed: Mapping not found.";
                        details = "Add a new @PostMapping(\"/lesson-plan\") method inside StudyController.java, compile and run the application, then run this test.";
                    }
                    break;
                }
                case "challenge-3": {
                    boolean foundField = false;
                    String fieldName = "";
                    for (java.lang.reflect.Field field : Flashcard.class.getDeclaredFields()) {
                        if (field.getName().equals("category") || field.getName().equals("difficulty")) {
                            foundField = true;
                            fieldName = field.getName();
                        }
                    }

                    if (foundField) {
                        passed = true;
                        message = "Challenge 3 Passed! Field '" + fieldName + "' detected in Flashcard record.";
                        details = "Reflection successfully detected your added field in Flashcard.java! Spring AI structured output can now map this property.";
                    } else {
                        passed = false;
                        message = "Challenge 3 Failed: Field not found.";
                        details = "Edit Flashcard.java and add a new String field named 'category' or 'difficulty' to the Flashcard record, compile the project, and run this test.";
                    }
                    break;
                }
                case "challenge-4": {
                    boolean isActive = java.util.Arrays.asList(this.environment.getActiveProfiles()).contains("openai");

                    if (isActive) {
                        passed = true;
                        message = "Challenge 4 Passed! Profile 'openai' is currently active.";
                        details = "The environment successfully shows 'openai' as an active profile. The real model connection is wired!";
                    } else {
                        passed = false;
                        message = "Challenge 4 Failed: Profile 'openai' is not active.";
                        details = "Start your Spring Boot server with the active profile set to 'openai' (e.g. using -Dspring-boot.run.profiles=openai), then run this test.";
                    }
                    break;
                }
                default: {
                    passed = false;
                    message = "Unknown Challenge ID";
                    details = "Challenge ID: " + challengeId;
                }
            }
        } catch (Exception e) {
            passed = false;
            message = "Verification Error";
            details = "An error occurred during reflection: " + e.getMessage();
        }

        return Map.of(
                "challengeId", challengeId,
                "passed", passed,
                "message", message,
                "details", details
        );
    }

    @PostMapping("/explain")
    public ExplainResponse explain(@Valid @RequestBody ExplainRequest request) {
        return this.studyAiClient.explain(request);
    }

    @PostMapping("/quiz")
    public QuizResponse quiz(@Valid @RequestBody QuizRequest request) {
        return this.studyAiClient.quiz(request);
    }

    @PostMapping("/flashcards")
    public FlashcardDeckResponse flashcards(@Valid @RequestBody FlashcardRequest request) {
        FlashcardDeckResponse response = this.studyAiClient.flashcards(request);
        try {
            User user = getCurrentUser();
            String cardsJson = objectMapper.writeValueAsString(response.cards());
            Optional<FlashcardDeck> existingDeck = flashcardDeckRepository.findByUserAndTopicIgnoreCase(user, request.topic());
            
            FlashcardDeck deck;
            if (existingDeck.isPresent()) {
                deck = existingDeck.get();
                deck.setLevel(request.level());
                deck.setCardsJson(cardsJson);
                deck.setCreatedAt(LocalDateTime.now());
            } else {
                deck = new FlashcardDeck(
                        UUID.randomUUID().toString(),
                        user,
                        request.topic(),
                        request.level(),
                        cardsJson,
                        LocalDateTime.now()
                );
            }
            flashcardDeckRepository.save(deck);
        } catch (Exception e) {
            // Log issue but do not block response
            System.err.println("Could not auto-save flashcard deck: " + e.getMessage());
        }
        return response;
    }

    @GetMapping("/flashcard-deck")
    public FlashcardDeckResponse getSavedFlashcardDeck(@RequestParam String topic) {
        User user = getCurrentUser();
        FlashcardDeck deck = flashcardDeckRepository.findByUserAndTopicIgnoreCase(user, topic)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No saved deck found for topic: " + topic));
        try {
            List<Flashcard> cards = objectMapper.readValue(deck.getCardsJson(), new TypeReference<List<Flashcard>>() {});
            return new FlashcardDeckResponse("database", deck.getTopic(), deck.getLevel(), cards);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to parse saved cards: " + e.getMessage());
        }
    }

    @GetMapping("/progress")
    public StudyProgressResponse getProgress() {
        User user = getCurrentUser();
        Optional<StudyProgress> progressOpt = studyProgressRepository.findByUser(user);
        if (progressOpt.isEmpty()) {
            return new StudyProgressResponse(0, 0, 0, Collections.emptyMap());
        }
        StudyProgress progress = progressOpt.get();
        try {
            Map<String, Object> moduleProgress = objectMapper.readValue(progress.getModuleProgressJson(), new TypeReference<Map<String, Object>>() {});
            return new StudyProgressResponse(
                    progress.getExplains(),
                    progress.getQuizzes(),
                    progress.getFlashcards(),
                    moduleProgress
            );
        } catch (Exception e) {
            return new StudyProgressResponse(progress.getExplains(), progress.getQuizzes(), progress.getFlashcards(), Collections.emptyMap());
        }
    }

    @PostMapping("/progress")
    public StudyProgressResponse updateProgress(@Valid @RequestBody StudyProgressRequest request) {
        User user = getCurrentUser();
        Optional<StudyProgress> existingProgress = studyProgressRepository.findByUser(user);
        
        String progressJson;
        try {
            progressJson = objectMapper.writeValueAsString(request.moduleProgress());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid module progress data");
        }

        StudyProgress progress;
        if (existingProgress.isPresent()) {
            progress = existingProgress.get();
            progress.setExplains(request.explains());
            progress.setQuizzes(request.quizzes());
            progress.setFlashcards(request.flashcards());
            progress.setModuleProgressJson(progressJson);
        } else {
            progress = new StudyProgress(
                    UUID.randomUUID().toString(),
                    user,
                    request.explains(),
                    request.quizzes(),
                    request.flashcards(),
                    progressJson
            );
        }

        studyProgressRepository.save(progress);
        return new StudyProgressResponse(
                progress.getExplains(),
                progress.getQuizzes(),
                progress.getFlashcards(),
                request.moduleProgress()
        );
    }

    @PostMapping("/quiz-result")
    public QuizResultResponse saveQuizResult(@Valid @RequestBody QuizResultRequest request) {
        User user = getCurrentUser();
        QuizResult result = new QuizResult(
                UUID.randomUUID().toString(),
                user,
                request.topic(),
                request.score(),
                request.totalQuestions(),
                LocalDateTime.now()
        );
        quizResultRepository.save(result);
        return new QuizResultResponse(
                result.getId(),
                result.getTopic(),
                result.getScore(),
                result.getTotalQuestions(),
                result.getCompletedAt()
        );
    }

    @GetMapping("/quiz-results")
    public List<QuizResultResponse> getQuizResults() {
        User user = getCurrentUser();
        return quizResultRepository.findByUserOrderByCompletedAtDesc(user)
                .stream()
                .map(r -> new QuizResultResponse(r.getId(), r.getTopic(), r.getScore(), r.getTotalQuestions(), r.getCompletedAt()))
                .collect(Collectors.toList());
    }

    private List<String> profiles() {
        String[] activeProfiles = this.environment.getActiveProfiles();
        if (activeProfiles.length > 0) {
            return Arrays.asList(activeProfiles);
        }
        return Arrays.asList(this.environment.getDefaultProfiles());
    }
}
