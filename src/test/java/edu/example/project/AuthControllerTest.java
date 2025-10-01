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

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
    void whenUserSignsIn_thenUserIsAuthenticated() throws Exception {
        User user = new User("username", "password");
        registrationService.registerUser(user);
        mockMvc.perform(
                post("/auth/sign-in").param("username", "username")
                        .param("password", "password")
        ).andExpect(authenticated());
    }

    @WithAnonymousUser
    @Test
    void whenNotRegisteredUserSignsIn_thenUserIsUnauthenticated() throws Exception {
        mockMvc.perform(
                post("/auth/sign-in").param("username", "username")
                        .param("password", "password")
        ).andExpect(unauthenticated());
    }

    /**
     * Правила валидации:
     * 1. Логин должен быть уникальным or 409
     * 2. Не должен содержать специальных символов или пробелов or 400
     * 3. Не должен быть длиннее 20 символов или короче 4 or 400
     *
     * 4. Пароль не должен быть длиннее 20 символов или короче 8
     */

//    @Test
//    void whenSignUpWithNotUniqueUsername_thenConflictStatus() throws Exception {
//        registrationService.registerUser(new User("user", "password"));
//        mockMvc.perform(
//                post("/auth/sign-up").param("username", "user")
//                        .param("password", "password")
//        ).andExpect(status().isConflict());
//    }
//
//    @Test
//    void whenSignUpWithTooLongUsername_thenBadRequestStatus() throws Exception {
//        String longUsername = "xjksnskasjhfauiwoqrqwoiww";
//        mockMvc.perform(
//                post("/auth/sign-up").param("username", longUsername)
//                        .param("password", "password")
//        ).andExpect(status().isBadRequest());
//    }
//
//    @Test
//    void whenSignUpWithTooShortUsername_thenBadRequestStatus() throws Exception {
//        String shortUsername = "xxx";
//        mockMvc.perform(
//                post("/auth/sign-up").param("username", shortUsername)
//                        .param("password", "password")
//        ).andExpect(status().isBadRequest());
//    }
//
//    @Test
//    void whenSignUpWithTooLongPassword_thenBadRequestStatus() throws Exception {
//        String longPassword = "xjksnskasjhfauiwoqrqwoiww";
//        mockMvc.perform(
//                post("/auth/sign-up").param("username", "username")
//                        .param("password", longPassword)
//        ).andExpect(status().isBadRequest());
//    }
//
//    @Test
//    void whenSignUpWithTooShortPassword_thenBadRequestStatus() throws Exception {
//        String shortPassword = "xxx";
//        mockMvc.perform(
//                post("/auth/sign-up").param("username", "username")
//                        .param("password", shortPassword)
//        ).andExpect(status().isBadRequest());
//    }

}
