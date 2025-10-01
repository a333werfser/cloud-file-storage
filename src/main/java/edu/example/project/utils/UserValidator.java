package edu.example.project.utils;

import edu.example.project.model.User;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

public class UserValidator implements Validator {

    private static final int MAXIMUM_PASSWORD_LENGTH = 20;

    private static final int MAXIMUM_USERNAME_LENGTH = 20;

    private static final int MINIMUM_PASSWORD_LENGTH = 8;

    private static final int MINIMUM_USERNAME_LENGTH = 4;

    @Override
    public boolean supports(Class<?> clazz) {
        return User.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        User user = (User) target;

        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "username", "username.emptyOrWhitespace");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "password", "password.emptyOrWhitespace");

        if (user.getUsername().matches("[^A-Za-z0-9]+")) {
            errors.rejectValue("username", "InvalidUsername");
        }
        if (user.getUsername().length() < MINIMUM_USERNAME_LENGTH || user.getUsername().length() > MAXIMUM_USERNAME_LENGTH) {
            errors.rejectValue("username", "InvalidUsernameLength");
        }
        if (user.getPassword().length() < MINIMUM_PASSWORD_LENGTH || user.getPassword().length() > MAXIMUM_PASSWORD_LENGTH) {
            errors.rejectValue("password", "InvalidPasswordLength");
        }
    }
}
