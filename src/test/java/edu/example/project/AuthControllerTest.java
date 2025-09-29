package edu.example.project;

import edu.example.project.model.User;
import edu.example.project.repository.UserRepository;
import edu.example.project.service.RegistrationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@ActiveProfiles("test")
@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
public class AuthControllerTest {

    @Autowired
    UserRepository userRepository;

    @Autowired
    RegistrationService registrationService;

    @Autowired
    MockMvc mockMvc;

    @BeforeEach
    void resetDatabase() {
        userRepository.deleteAll();
    }

    @Test
    void whenNoAuthenticatedUser_thenUnauthorizedStatus() throws Exception {
        mockMvc.perform(get("/test-endpoint")).andExpect(unauthenticated());
    }

    @WithMockUser
    @Test
    void whenAuthenticatedUser_thenActualStatus() throws Exception {
        mockMvc.perform(get("/test-endpoint")).andExpect(authenticated());
    }

    @WithMockUser
    @Test
    void whenUserLogsOut_thenUserIsUnauthenticated() throws Exception {
        mockMvc.perform(post("/auth/sign-out")).andExpect(unauthenticated());
    }

    @WithAnonymousUser
    @Test
    void whenUserSignsUp_thenUserIsAuthenticated() throws Exception {
        mockMvc.perform(get("/test-endpoint")).andExpect(unauthenticated());
        mockMvc.perform(
                post("/auth/sign-up").param("username", "username")
                        .param("password", "password")
        ).andExpect(authenticated());
    }

    @WithAnonymousUser
    @Test
    void whenUserSignIn_thenUserIsAuthenticated() throws Exception {
        User user = new User("username", "password");
        registrationService.registerUser(user);
        mockMvc.perform(
                post("/auth/sign-in").param("username", "username")
                        .param("password", "password")
        ).andExpect(authenticated());
    }

}
