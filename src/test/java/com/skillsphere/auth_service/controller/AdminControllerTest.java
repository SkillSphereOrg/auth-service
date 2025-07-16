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
        // Create a user
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        java.util.Map.of("username", "testuser2", "email", "testuser2@example.com", "password",
                                "testpass2"))))
                .andExpect(status().isOk());
        // Get user id
        String usersJson = mockMvc.perform(get("/api/admin/users")
                .header("Authorization", "Bearer " + adminJwt))
                .andReturn().getResponse().getContentAsString();
        long userId = objectMapper.readTree(usersJson)
                .findValuesAsText("username").contains("testuser2")
                        ? objectMapper.readTree(usersJson)
                                .findValues("id").get(
                                        objectMapper.readTree(usersJson).findValuesAsText("username")
                                                .indexOf("testuser2"))
                                .asLong()
                        : -1;
        // Assign ROLE_ADMIN
        mockMvc.perform(put("/api/admin/user/" + userId + "/roles")
                .header("Authorization", "Bearer " + adminJwt)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Set.of("ROLE_ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roles").isArray());
    }
}