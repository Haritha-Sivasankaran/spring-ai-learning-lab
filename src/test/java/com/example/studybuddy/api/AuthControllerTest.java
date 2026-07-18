package com.example.studybuddy.api;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.studybuddy.dto.LoginRequest;
import com.example.studybuddy.dto.RegisterRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
public class AuthControllerTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();
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
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("integration@example.com"))
                .andExpect(jsonPath("$.name").value("Integration Student"));

        // 2. Login with registered user
        LoginRequest loginReq = new LoginRequest(
                "integration@example.com",
                "securePassword123"
        );

        MockHttpSession session = (MockHttpSession) mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("integration@example.com"))
                .andExpect(jsonPath("$.name").value("Integration Student"))
                .andReturn().getRequest().getSession();

        // 3. Verify /me endpoint works with valid session
        mockMvc.perform(get("/api/auth/me")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("integration@example.com"));

        // 4. Verify access allowed to study endpoints with valid session
        mockMvc.perform(post("/api/study/explain")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"question\":\"What is Spring AI?\", \"level\":\"beginner\", \"goal\":\"explain\"}"))
                .andExpect(status().isOk());

        // 5. Logout
        mockMvc.perform(post("/api/auth/logout")
                        .session(session))
                .andExpect(status().isOk());

        // 6. Verify session is cleared
        mockMvc.perform(get("/api/auth/me")
                        .session(session))
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
