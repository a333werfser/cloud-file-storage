package edu.example.project.controller;

import edu.example.project.model.User;
import edu.example.project.service.RegistrationService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private RegistrationService registrationService;

    @Autowired
    public AuthController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @PostMapping("/sign-up")
    public String signUp(@RequestParam String username, @RequestParam String password, HttpServletResponse response) {
        User user = new User(username, password);
        registrationService.registerUser(user);

        response.setStatus(201);
        return user.getUsername();
    }

}
