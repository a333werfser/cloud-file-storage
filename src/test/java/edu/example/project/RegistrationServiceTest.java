package edu.example.project;

import edu.example.project.model.User;
import edu.example.project.repository.UserRepository;
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
    UserRepository userRepository;

    @BeforeEach
    void resetDatabase() {
        userRepository.deleteAll();
    }

    @Test
    void whenNotUniqueUsername_thenThrowsException() {
        String notUniqueUsername = "username";
        User user1 = new User(notUniqueUsername, "password");
        User user2 = new User(notUniqueUsername, "password");

        userRepository.save(user1);
        assertThrows(DataIntegrityViolationException.class, () -> userRepository.save(user2));
    }

}
