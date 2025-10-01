package edu.example.project;

import edu.example.project.model.TestObject;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class SomeTest {

    private static Validator validator;

    @BeforeAll
    public static void setupValidator() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    public void testValidation() {
        TestObject testObject = new TestObject("aaa aaaa    ", "1234");

        Set<ConstraintViolation<TestObject>> violations = validator.validate(testObject);
        assertEquals("nonsensetext" , violations.iterator().next().getMessage());
    }

}
