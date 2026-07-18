package com.example.studybuddy.api;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.studybuddy.ai.StudyAiClient;
import com.example.studybuddy.dto.LoginRequest;
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

@SpringBootTest
@ActiveProfiles("openai")
public class AuthControllerTest {

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

        // Stub the studyAiClient methods to return mock responses
        Mockito.when(studyAiClient.explain(Mockito.any()))
               .thenReturn(new ExplainResponse("mock", "Test explanation answer", List.of("Next Step")));
    }

    @Test
    public void testAuthenticationRequiredForStudyEndpoints() throws Exception {
        mockMvc.perform(post("/api/study/explain")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"question\":\"What is Spring AI?\", \"level\":\"beginner\", \"goal\":\"explain\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testSuccessfulRegistrationAndLogin() throws Exception {
        RegisterRequest registerReq = new RegisterRequest(
                "integration@example.com",
                "securePassword123",
                "Integration Student"
        );

        // 1. Register new user
        MvcResult registerResult = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("integration@example.com"))
                .andExpect(jsonPath("$.name").value("Integration Student"))
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andReturn();

        // 2. Login with registered user
        LoginRequest loginReq = new LoginRequest(
                "integration@example.com",
                "securePassword123"
        );

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("integration@example.com"))
                .andExpect(jsonPath("$.name").value("Integration Student"))
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andReturn();

        // Parse token
        String responseContent = loginResult.getResponse().getContentAsString();
        UserResponse loginResponse = objectMapper.readValue(responseContent, UserResponse.class);
        String token = loginResponse.token();

        // 3. Verify /me endpoint works with valid token
        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("integration@example.com"));

        // 4. Verify access allowed to study endpoints with valid token
        mockMvc.perform(post("/api/study/explain")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"question\":\"What is Spring AI?\", \"level\":\"beginner\", \"goal\":\"explain\"}"))
                .andExpect(status().isOk());

        // 5. Verify invalid token returns unauthorized
        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer invalid-token-value"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testInvalidLoginCredentials() throws Exception {
        LoginRequest loginReq = new LoginRequest(
                "nonexistent@example.com",
                "wrongPassword"
        );

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testRegistrationValidationFailure() throws Exception {
        RegisterRequest registerReq = new RegisterRequest(
                "not-an-email", // invalid email format
                "",             // empty password
                ""              // empty name
            );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerReq)))
                .andExpect(status().isBadRequest());
    }
}
