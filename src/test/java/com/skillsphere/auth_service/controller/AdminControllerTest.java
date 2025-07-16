package com.skillsphere.auth_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class AdminControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    private String adminJwt;

    @BeforeEach
    void setUp() throws Exception {
        // Login as admin
        String response = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        java.util.Map.of("username", "admin", "password", "admin123"))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        adminJwt = objectMapper.readTree(response).get("token").asText();
    }

    @Test
    void listUsersAsAdmin() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                .header("Authorization", "Bearer " + adminJwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").exists());
    }

    @Test
    void assignRolesToUser() throws Exception {
        String unique = String.valueOf(System.currentTimeMillis());
        String username = "testuser2_" + unique;
        String email = "testuser2_" + unique + "@example.com";
        // Register user
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        java.util.Map.of("username", username, "email", email, "password",
                                "testpass2"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User registered successfully"));
        // Get user id
        String usersJson = mockMvc.perform(get("/api/admin/users")
                .header("Authorization", "Bearer " + adminJwt))
                .andReturn().getResponse().getContentAsString();
        com.fasterxml.jackson.databind.JsonNode usersNode = objectMapper.readTree(usersJson);
        int idx = -1;
        for (int i = 0; i < usersNode.size(); i++) {
            if (usersNode.get(i).get("username").asText().equals(username)) {
                idx = i;
                break;
            }
        }
        org.junit.jupiter.api.Assertions.assertTrue(idx != -1, username + " not found in user list");
        long userId = usersNode.get(idx).get("id").asLong();
        // Assign ROLE_ADMIN
        mockMvc.perform(put("/api/admin/user/" + userId + "/roles")
                .header("Authorization", "Bearer " + adminJwt)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Set.of("ROLE_ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roles").isArray());
    }
}