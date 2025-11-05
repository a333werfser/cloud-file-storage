package edu.example.project;

import edu.example.project.exception.NotUniqueUsernameException;
import edu.example.project.model.User;
import edu.example.project.repository.UserRepository;
import edu.example.project.service.RegistrationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.assertThrows;

@ActiveProfiles("test")
@Testcontainers
@SpringBootTest
public class RegistrationServiceTest {

    @Autowired
    RegistrationService registrationService;

    @Autowired
    UserRepository userRepository;

    @BeforeEach
    void resetDatabase() {
        userRepository.deleteAll();
    }

    @Test
    void whenSaveWithNotUniqueUsername_thenRepositoryThrowsException() {
        String notUnique = "username";
        User user1 = new User(notUnique, "password");
        User user2 = new User(notUnique, "password");
        userRepository.save(user1);
        assertThrows(DataIntegrityViolationException.class, () -> userRepository.save(user2));
    }

    @Test
    void whenRegisterWithNotUniqueUsername_thenServiceThrowsCustomException() {
        String notUnique = "username";
        User user1 = new User(notUnique, "password");
        User user2 = new User(notUnique, "password");
        registrationService.registerUser(user1);
        assertThrows(NotUniqueUsernameException.class, () -> registrationService.registerUser(user2));
    }

}
