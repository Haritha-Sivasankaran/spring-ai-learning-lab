package com.example.studybuddy.api;

import com.example.studybuddy.ai.StudyAiClient;
import jakarta.validation.Valid;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/study")
public class StudyController {

    private final StudyAiClient studyAiClient;
    private final Environment environment;

    public StudyController(StudyAiClient studyAiClient, Environment environment) {
        this.studyAiClient = studyAiClient;
        this.environment = environment;
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
        return this.studyAiClient.flashcards(request);
    }

    private List<String> profiles() {
        String[] activeProfiles = this.environment.getActiveProfiles();
        if (activeProfiles.length > 0) {
            return Arrays.asList(activeProfiles);
        }
        return Arrays.asList(this.environment.getDefaultProfiles());
    }
}
