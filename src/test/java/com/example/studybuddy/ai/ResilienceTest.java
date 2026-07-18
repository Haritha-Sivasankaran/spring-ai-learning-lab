package com.example.studybuddy.ai;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.example.studybuddy.api.ExplainRequest;
import com.example.studybuddy.api.ExplainResponse;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("openai")
public class ResilienceTest {

    @Autowired
    private StudyAiClient studyAiClient;

    @Autowired
    private ChatClient mockChatClient;

    @TestConfiguration
    public static class TestConfig {
        
        @Bean
        public ChatClient mockChatClient() {
            return mock(ChatClient.class);
        }

        @Bean
        @Primary
        public ChatClient.Builder chatClientBuilder(ChatClient mockChatClient) {
            ChatClient.Builder builder = mock(ChatClient.Builder.class);
            when(builder.defaultSystem(any(String.class))).thenReturn(builder);
            when(builder.build()).thenReturn(mockChatClient);
            return builder;
        }
    }

    @Test
    public void testFallbackTriggeredOnOpenAiFailure() {
        ChatClient.ChatClientRequestSpec requestSpec = mock(ChatClient.ChatClientRequestSpec.class);
        when(mockChatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.user(any(java.util.function.Consumer.class))).thenReturn(requestSpec);
        when(requestSpec.call()).thenThrow(new RuntimeException("OpenAI API is down"));

        // Call explain - AOP should intercept the RuntimeException and return the fallback response
        ExplainResponse response = studyAiClient.explain(new ExplainRequest("What is Spring AI?", "beginner", "explain"));

        // Verify fallback was executed
        assertThat(response.provider()).isEqualTo("spring-ai-openai-fallback");
        assertThat(response.answer()).contains("The AI tutor is currently unavailable");
        assertThat(response.answer()).contains("OpenAI API is down");
    }
}
