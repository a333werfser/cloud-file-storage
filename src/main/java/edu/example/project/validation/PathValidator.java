package edu.example.project.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PathValidator implements ConstraintValidator<ValidPath, String> {

    @Override
    public void initialize(ValidPath constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(String object, ConstraintValidatorContext context) {
        if (object == null || object.isEmpty()) {
            return true;
        }
        Pattern pattern = Pattern.compile("^[a-zA-Z0-9/_\\-.]+$");
        Matcher matcher = pattern.matcher(object);
        return !object.contains("//") && !object.contains("..") && matcher.matches();
    }

}
