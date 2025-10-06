package edu.example.project.service;

import edu.example.project.exception.NotUniqueUsernameException;
import edu.example.project.model.User;
import edu.example.project.repository.UserRepository;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class RegistrationService {

    private UserRepository userRepository;

    private PasswordEncoder passwordEncoder;

    @Autowired
    public RegistrationService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void registerUser(User user) {
        String password = user.getPassword();
        String encodedPassword = passwordEncoder.encode(password);
        user.setPassword(encodedPassword);
        try {
            userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            if (e.getCause() instanceof ConstraintViolationException) {
                ConstraintViolationException cve = (ConstraintViolationException) e.getCause();
                String constraintName = cve.getConstraintName();
                if (constraintName.equals("users_username_key")) {
                    throw new NotUniqueUsernameException("Username already exists", e);
                }
            } else {
                throw e;
            }
        }
    }

}
