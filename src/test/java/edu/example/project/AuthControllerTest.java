package edu.example.project;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.example.project.dto.AuthRequestDto;
import edu.example.project.dto.ResponseMessageDto;
import edu.example.project.model.User;
import edu.example.project.repository.UserRepository;
import edu.example.project.service.RegistrationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
        mockMvc.perform(post("/auth/sign-out")).andExpect(unauthenticated());
    }

    @WithAnonymousUser
    @Test
    void whenUserSignsUp_thenUserBecomesAuthenticated() throws Exception {
        String json = objectMapper.writeValueAsString(new AuthRequestDto("username", "password"));

        mockMvc.perform(get("/test-endpoint")).andExpect(unauthenticated());
        mockMvc.perform(
                post("/auth/sign-up").contentType(MediaType.APPLICATION_JSON_VALUE).content(json)
        ).andExpect(authenticated());
    }

    @WithAnonymousUser
    @Test
    void whenUserSignsIn_thenUserBecomesAuthenticated() throws Exception {
        User user = new User("username", "password");
        String json = objectMapper.writeValueAsString(new AuthRequestDto(user.getUsername(), user.getPassword()));

        registrationService.registerUser(user);
        mockMvc.perform(
                post("/auth/sign-in").contentType(MediaType.APPLICATION_JSON_VALUE).content(json)
        ).andExpect(authenticated());
    }

    @WithAnonymousUser
    @Test
    void whenNotRegisteredUserSignsIn_thenUserIsUnauthenticated() throws Exception {
        String json = objectMapper.writeValueAsString(new AuthRequestDto("username", "password"));

        mockMvc.perform(
                post("/auth/sign-in").contentType(MediaType.APPLICATION_JSON_VALUE).content(json)
        ).andExpect(unauthenticated());
    }

    @Test
    void whenUserSignsUpWithInvalidCredentials_thenBadRequest() throws Exception {
        String json = objectMapper.writeValueAsString(new AuthRequestDto("@.//", "pasw  pasw"));

        mockMvc.perform(
                post("/auth/sign-up").contentType(MediaType.APPLICATION_JSON_VALUE).content(json)
        ).andExpectAll(status().isBadRequest(), content().contentType(MediaType.APPLICATION_JSON_VALUE));
    }

    // неуникальный логин -> статус код и месседж
    // кастомная 500 ошибка
    // логинится незарегистрированный пользователь

    @WithAnonymousUser
    @Test
    void whenNotRegisteredUserSignsIn_thenCorrectJsonMessageAndUnauthenticatedStatus() throws Exception {
        String json = objectMapper.writeValueAsString(new AuthRequestDto("username", "password"));
        String mockedJson = objectMapper.writeValueAsString(new ResponseMessageDto("User not found"));
        String actualJson;

        MvcResult results = mockMvc.perform(
                post("/auth/sign-in").contentType(MediaType.APPLICATION_JSON_VALUE).content(json)
        ).andExpectAll(status().isUnauthorized(), content().contentType(MediaType.APPLICATION_JSON_VALUE)).andReturn();
        actualJson = results.getResponse().getContentAsString();
        assertTrue(mockedJson.equals(actualJson));
    }

}
