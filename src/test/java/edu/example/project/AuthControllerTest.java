package edu.example.project;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.example.project.dto.AuthRequest;
import edu.example.project.dto.PathRequest;
import edu.example.project.dto.ResponseMessage;
import edu.example.project.model.User;
import edu.example.project.repository.UserRepository;
import edu.example.project.service.RegistrationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.multipart.MultipartFile;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
public class AuthControllerTest {

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RegistrationService registrationService;

    @BeforeEach
    void resetDatabase() {
        userRepository.deleteAll();
    }

    @Test
    void whenUnauthenticatedUser_thenUnauthenticated() throws Exception {
        mockMvc.perform(get("/test-endpoint")).andExpect(unauthenticated());
    }

    @WithMockUser
    @Test
    void whenAuthenticatedUser_thenAuthenticated() throws Exception {
        mockMvc.perform(get("/test-endpoint")).andExpect(authenticated());
    }

    @WithMockUser
    @Test
    void whenUserLogsOut_thenUserBecomesUnauthenticated() throws Exception {
        mockMvc.perform(post("/api/auth/sign-out")).andExpect(unauthenticated());
    }

    @WithAnonymousUser
    @Test
    void whenUserSignsUp_thenUserBecomesAuthenticated() throws Exception {
        String json = objectMapper.writeValueAsString(new AuthRequest("username", "password"));

        mockMvc.perform(get("/test-endpoint")).andExpect(unauthenticated());
        mockMvc.perform(
                post("/api/auth/sign-up").contentType(MediaType.APPLICATION_JSON_VALUE).content(json)
        ).andExpect(authenticated());
    }

    @WithAnonymousUser
    @Test
    void whenUserSignsIn_thenUserBecomesAuthenticated() throws Exception {
        User user = new User("username", "password");
        String json = objectMapper.writeValueAsString(new AuthRequest(user.getUsername(), user.getPassword()));

        registrationService.registerUser(user);
        mockMvc.perform(
                post("/api/auth/sign-in").contentType(MediaType.APPLICATION_JSON_VALUE).content(json)
        ).andExpect(authenticated());
    }

    @WithAnonymousUser
    @Test
    void whenNotRegisteredUserSignsIn_thenUserIsUnauthenticated() throws Exception {
        String json = objectMapper.writeValueAsString(new AuthRequest("username", "password"));

        mockMvc.perform(
                post("/api/auth/sign-in").contentType(MediaType.APPLICATION_JSON_VALUE).content(json)
        ).andExpect(unauthenticated());
    }

    @Test
    void whenUserSignsUpWithInvalidCredentials_thenBadRequest() throws Exception {
        String json = objectMapper.writeValueAsString(new AuthRequest("@.//", "pasw  pasw"));

        mockMvc.perform(
                post("/api/auth/sign-up").contentType(MediaType.APPLICATION_JSON_VALUE).content(json)
        ).andExpectAll(status().isBadRequest(), content().contentType(MediaType.APPLICATION_JSON_VALUE));
    }

    @WithAnonymousUser
    @Test
    void whenNotRegisteredUserSignsIn_thenCorrectJsonMessageAndUnauthenticatedStatus() throws Exception {
        String json = objectMapper.writeValueAsString(new AuthRequest("username", "password"));
        String mockedJson = objectMapper.writeValueAsString(new ResponseMessage("User not found"));
        String actualJson;

        MvcResult results = mockMvc.perform(
                post("/api/auth/sign-in").contentType(MediaType.APPLICATION_JSON_VALUE).content(json)
        ).andExpectAll(status().isUnauthorized(), content().contentType(MediaType.APPLICATION_JSON_VALUE)).andReturn();
        actualJson = results.getResponse().getContentAsString();
        assertEquals(mockedJson, actualJson);
    }

    @WithMockUser
    @Test
    void shouldReturnBadRequest() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "file".getBytes());

        mockMvc.perform(
                multipart("/api/resource").file(file).param("path", "/")
        ).andExpect(status().isBadRequest());
    }

}
