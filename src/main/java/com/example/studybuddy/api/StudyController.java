package com.example.studybuddy.api;

import com.example.studybuddy.ai.StudyAiClient;
import jakarta.validation.Valid;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
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
