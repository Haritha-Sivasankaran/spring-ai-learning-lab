# Skill: Spring AI Integration Patterns

## Metadata
- ID: spring-ai-core-patterns
- Title: Spring AI ChatClient and Jackson Record Deserialization Rules
- Version: 1.0.0
- Harvested: 2026-07-17
- Source: Spring AI Learning Lab
- Scope: Project-wide Java backend LLM configurations

## Context
When working with AI integrations in Spring Boot, the agent must avoid using legacy constructors (e.g. ChatClient constructors) and instead construct instances via ChatClient.Builder. It must also map structured JSON outputs to Java Records.

## Distilled Patterns & Rules

### 1. Fluent ChatClient Builders
Avoid legacy constructors. Always build ChatClient instances using ChatClient.Builder injected via constructor parameters:
```java
@Service
public class ChatService {
    private final ChatClient chatClient;

    public ChatService(ChatClient.Builder builder) {
        this.chatClient = builder
            .defaultSystem("Injected System Context")
            .build();
    }
}
```

### 2. Prompt Templating and Parameter Mapping
Utilize PromptTemplates for mapping dynamic prompts. Bind keys to maps safely:
```java
PromptTemplate template = new PromptTemplate("Explain {topic} in detail.");
Prompt prompt = template.create(Map.of("topic", topic));
String response = this.chatClient.prompt(prompt).call().content();
```

### 3. Structured Model Outputs
Map output JSON shapes directly to records using Jackson schema generators via .entity() calls:
```java
public record StudyCard(String front, String back, String hint) {}

public StudyCard generateCard(String topic) {
    return this.chatClient.prompt()
        .user("Create flashcard for: " + topic)
        .call()
        .entity(StudyCard.class);
}
```

### 4. Profile Management
Always ensure the active profile is set to 'openai' for production integration, or 'default' / 'mock' for local offline testing.

## Lifecycle Details
- Graduation Status: Graduate
- Usage Ledger: Pre-bundled with workspace.
- Autoharness ID: auto-spring-ai-study-buddy
