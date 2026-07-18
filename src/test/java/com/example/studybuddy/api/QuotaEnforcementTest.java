package com.example.studybuddy.api;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.studybuddy.ai.StudyAiClient;
import com.example.studybuddy.api.ExplainResponse;
import com.example.studybuddy.dto.RegisterRequest;
import com.example.studybuddy.dto.UserResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest(properties = "app.quota.daily-limit=2")
@ActiveProfiles("openai")
public class QuotaEnforcementTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private StudyAiClient studyAiClient;

    @BeforeEach
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        Mockito.when(studyAiClient.explain(Mockito.any()))
               .thenReturn(new ExplainResponse("mock", "Test explanation answer", List.of("Next Step")));
    }

    @Test
    public void testDailyQuotaEnforced() throws Exception {
        RegisterRequest registerReq = new RegisterRequest(
                "quota@example.com",
                "password123",
                "Quota Student"
        );

        // Register and get token
        MvcResult registerResult = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerReq)))
                .andExpect(status().isCreated())
                .andReturn();

        String responseContent = registerResult.getResponse().getContentAsString();
        UserResponse userResponse = objectMapper.readValue(responseContent, UserResponse.class);
        String token = userResponse.token();

        String payload = "{\"question\":\"What is Spring AI?\", \"level\":\"beginner\", \"goal\":\"explain\"}";

        // Call 1: Allowed
        mockMvc.perform(post("/api/study/explain")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk());

        // Call 2: Allowed
        mockMvc.perform(post("/api/study/explain")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk());

        // Call 3: Exceeded (limit is 2)
        mockMvc.perform(post("/api/study/explain")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.status").value(429))
                .andExpect(jsonPath("$.error").value("Too Many Requests"))
                .andExpect(jsonPath("$.message").value("You have exceeded your daily quota of 2 AI calls. Please try again tomorrow."));
    }
}
