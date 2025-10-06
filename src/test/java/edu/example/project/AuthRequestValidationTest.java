package edu.example.project;

import edu.example.project.dto.AuthRequestDto;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotNull;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class AuthRequestValidationTest {

    static Validator validator;

    @BeforeAll
    static void beforeAll() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @ParameterizedTest
    @ValueSource(
            strings = {"@..//", "....", "", " ", "    ", "zxc zxc zxc", "  user", "user   ", "user.user", "user-a"}
    )
    void whenIncorrectUsername_thenViolationsSetIsNotEmpty(String incorrectUsername) {
        String password = "valid.password";
        Set<ConstraintViolation<AuthRequestDto>> violations = validator.validate(new AuthRequestDto(incorrectUsername, password));
        assertTrue(!violations.isEmpty());
    }

    @ParameterizedTest
    @ValueSource(
            strings = {"user", "user123", "user_user", "USER", "ADMINISTRAtor", "XlEB4IiIeeeek__"}
    )
    void whenCorrectUsername_thenViolationsSetIsEmpty(String correctUsername) {
        String password = "valid.password";
        Set<ConstraintViolation<AuthRequestDto>> violations = validator.validate(new AuthRequestDto(correctUsername, password));
        assertTrue(violations.isEmpty());
    }

    @ParameterizedTest
    @ValueSource(
            strings = {"password  ", "          ", "      passw", "parol parol", "1234passw   psdsa"}
    )
    void whenIncorrectPassword_thenViolationsSetIsNotEmpty(String incorrectPassword) {
        String username = "valid_user";
        Set<ConstraintViolation<AuthRequestDto>> violations = validator.validate(new AuthRequestDto(username, incorrectPassword));
        assertTrue(!violations.isEmpty());
    }

    @ParameterizedTest
    @ValueSource(
            strings = {"12345678", "!@#strong_pass...", "!!!!!!!!!!!!!", "...........", "hackMeLittleJerk"}
    )
    void whenCorrectPassword_thenViolationsSetIsEmpty(String correctPassword) {
        String username = "valid_user";
        Set<ConstraintViolation<AuthRequestDto>> violations = validator.validate(new AuthRequestDto(username, correctPassword));
        assertTrue(violations.isEmpty());
    }

    @Test
    void whenUsernameAndPasswordAreNull_thenSetPopulatesWithNotNullViolations() {
        Set<ConstraintViolation<AuthRequestDto>> violations = validator.validate(new AuthRequestDto(null, null));
        for (ConstraintViolation<AuthRequestDto> violation : violations) {
            assertTrue(violation.getConstraintDescriptor().getAnnotation() instanceof NotNull);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = { "xxx", "yyyyyyyyyyyyyyyyyyyyy" } )
    void whenIncorrectUsernameLength_thenViolationsSetIsNotEmpty(String incorrectUsername) {
        String password = "valid.password";
        Set<ConstraintViolation<AuthRequestDto>> violations = validator.validate(new AuthRequestDto(incorrectUsername, password));
        assertTrue(!violations.isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = { "xxx", "yyyyyyyyyyyyyyyyyyyyy" } )
    void whenIncorrectPasswordLength_thenViolationsSetIsNotEmpty(String incorrectPassword) {
        String username = "valid_user";
        Set<ConstraintViolation<AuthRequestDto>> violations = validator.validate(new AuthRequestDto(username, incorrectPassword));
        assertTrue(!violations.isEmpty());
    }
}
