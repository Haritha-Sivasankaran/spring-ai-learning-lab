# Spring AI Learning Lab

A small Spring Boot + Spring AI project you can use for learning.

It has three features:

- Explain a concept in a tutor style.
- Generate a multiple-choice quiz as structured Java output.
- Generate flashcards as structured Java output.

The app runs in `mock` mode by default, so you can start it without an API key. When you are ready to call a real model, run it with the `openai` profile.

## What You Will Learn

- How a Spring Boot REST controller calls an AI service.
- How to use Spring AI `ChatClient`.
- How prompt parameters work with `.param(...)`.
- How `.entity(MyRecord.class)` maps model output into Java records.
- How Spring profiles can switch between a mock implementation and a real AI provider.

## Project Structure

```text
src/main/java/com/example/studybuddy
  StudyBuddyApplication.java
  api/
    StudyController.java
    request and response records
  ai/
    StudyAiClient.java
    MockStudyAiClient.java
    SpringAiStudyClient.java

src/main/resources
  application.yml
  application-openai.yml
  static/index.html
```

## Run In Mock Mode

```powershell
mvn spring-boot:run
```

Open:

```text
http://localhost:8080
```

Mock mode is useful for learning the controller, DTOs, and frontend without using any paid model calls.

## Run With OpenAI

PowerShell:

```powershell
$env:OPENAI_API_KEY="your-api-key"
mvn spring-boot:run "-Dspring-boot.run.profiles=openai"
```

Optional model override:

```powershell
$env:OPENAI_CHAT_MODEL="gpt-5-mini"
```

## Try The API

Use `requests.http`, or run:

```powershell
Invoke-RestMethod http://localhost:8080/api/study/status
```

Explain:

```powershell
Invoke-RestMethod `
  -Method Post `
  -ContentType "application/json" `
  -Uri http://localhost:8080/api/study/explain `
  -Body '{"question":"What is Spring AI ChatClient?","level":"beginner","goal":"explain it in an interview"}'
```

Quiz:

```powershell
Invoke-RestMethod `
  -Method Post `
  -ContentType "application/json" `
  -Uri http://localhost:8080/api/study/quiz `
  -Body '{"topic":"Spring AI structured output","difficulty":"beginner","numberOfQuestions":3}'
```

## Study Path

1. Start at `StudyController` and see the REST endpoints.
2. Open `StudyAiClient` and notice the interface boundary.
3. Read `MockStudyAiClient` to understand the app flow without AI.
4. Read `SpringAiStudyClient` to learn Spring AI `ChatClient`.
5. Change one prompt and compare mock vs `openai` behavior.
6. Add a new endpoint, for example `/api/study/lesson-plan`.

## AI Coder & AutoHarness Integration

This repository is optimized for autonomous AI coding agents like **Claude Code**. It pre-bundles distilled Spring AI development skills so that your agent doesn't hallucinate legacy configurations:

- **Pre-Bundled Skills**: Located in `.claude/skills/spring_ai_patterns.md`. It outlines rules for ChatClient building, structured record deserialization, and profile usage.
- **Tigerless Labs AutoHarness**: If you are using Claude Code locally, you can install the `autoharness` self-learning skill layer. It monitors your coding sessions, aggregates new skills, and prunes unused ones:
  ```bash
  # Inside Claude Code CLI:
  /plugin marketplace add tigerless-labs/autoharness
  /plugin install autoharness@autoharness
  /reload-plugins
  ```

## Official Docs Used

- Spring Boot 4.1.0 documentation: https://docs.spring.io/spring-boot/index.html
- Spring Boot web starter list: https://docs.spring.io/spring-boot/reference/using/build-systems.html
- Spring Boot system requirements: https://docs.spring.io/spring-boot/system-requirements.html
- Spring AI structured output: https://docs.spring.io/spring-ai/reference/api/structured-output-converter.html
