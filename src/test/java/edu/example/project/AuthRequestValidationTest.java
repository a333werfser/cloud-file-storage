package edu.example.project;

import edu.example.project.dto.AuthRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.constraints.NotNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class AuthRequestValidationTest {

    static ValidatorFactory factory;

    static Validator validator;

    @BeforeAll
    static void beforeAll() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @ParameterizedTest
    @ValueSource(
            strings = {"@..//", "....", "", " ", "    ", "zxc zxc zxc", "  user", "user   ", "user.user", "user-a"}
    )
    void whenIncorrectUsername_thenViolationsSetIsNotEmpty(String incorrectUsername) {
        String password = "valid.password";
        Set<ConstraintViolation<AuthRequest>> violations = validator.validate(new AuthRequest(incorrectUsername, password));
        assertFalse(violations.isEmpty());
    }

    @ParameterizedTest
    @ValueSource(
            strings = {"user", "user123", "user_user", "USER", "ADMINISTRAtor", "XlEB4IiIeeeek__"}
    )
    void whenCorrectUsername_thenViolationsSetIsEmpty(String correctUsername) {
        String password = "valid.password";
        Set<ConstraintViolation<AuthRequest>> violations = validator.validate(new AuthRequest(correctUsername, password));
        assertTrue(violations.isEmpty());
    }

    @ParameterizedTest
    @ValueSource(
            strings = {"password  ", "          ", "      passw", "parol parol", "1234passw   psdsa"}
    )
    void whenIncorrectPassword_thenViolationsSetIsNotEmpty(String incorrectPassword) {
        String username = "valid_user";
        Set<ConstraintViolation<AuthRequest>> violations = validator.validate(new AuthRequest(username, incorrectPassword));
        assertFalse(violations.isEmpty());
    }

    @ParameterizedTest
    @ValueSource(
            strings = {"12345678", "!@#strong_pass...", "!!!!!!!!!!!!!", "...........", "hackMeLittleJerk"}
    )
    void whenCorrectPassword_thenViolationsSetIsEmpty(String correctPassword) {
        String username = "valid_user";
        Set<ConstraintViolation<AuthRequest>> violations = validator.validate(new AuthRequest(username, correctPassword));
        assertTrue(violations.isEmpty());
    }

    @Test
    void whenUsernameAndPasswordAreNull_thenSetPopulatesWithNotNullViolations() {
        Set<ConstraintViolation<AuthRequest>> violations = validator.validate(new AuthRequest(null, null));
        for (ConstraintViolation<AuthRequest> violation : violations) {
            assertInstanceOf(NotNull.class, violation.getConstraintDescriptor().getAnnotation());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = { "xxx", "yyyyyyyyyyyyyyyyyyyyy" } )
    void whenIncorrectUsernameLength_thenViolationsSetIsNotEmpty(String incorrectUsername) {
        String password = "valid.password";
        Set<ConstraintViolation<AuthRequest>> violations = validator.validate(new AuthRequest(incorrectUsername, password));
        assertFalse(violations.isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = { "xxx", "yyyyyyyyyyyyyyyyyyyyy" } )
    void whenIncorrectPasswordLength_thenViolationsSetIsNotEmpty(String incorrectPassword) {
        String username = "valid_user";
        Set<ConstraintViolation<AuthRequest>> violations = validator.validate(new AuthRequest(username, incorrectPassword));
        assertFalse(violations.isEmpty());
    }

    @AfterAll
    static void afterAll() {
        if (factory != null) {
            factory.close();
        }
    }
}
