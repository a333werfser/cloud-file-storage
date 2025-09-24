package edu.example.project.controller;

import edu.example.project.dto.UserDto;
import edu.example.project.model.User;
import edu.example.project.service.RegistrationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private RegistrationService registrationService;

    private AuthenticationManager authenticationManager;

    private SecurityContextHolderStrategy securityContextHolderStrategy = SecurityContextHolder.getContextHolderStrategy();

    private LogoutHandler logoutHandler = new SecurityContextLogoutHandler();

    private SecurityContextRepository securityContextRepository;

    @Autowired
    public AuthController(RegistrationService registrationService,
                          AuthenticationManager authenticationManager,
                          SecurityContextRepository securityContextRepository) {
        this.registrationService = registrationService;
        this.authenticationManager = authenticationManager;
        this.securityContextRepository = securityContextRepository;
    }

    @PostMapping("/sign-up")
    public ResponseEntity<UserDto> performSignUp(@RequestParam String username, @RequestParam String password, UserDto userDto,
                                                 HttpServletRequest request, HttpServletResponse response) {
        User user = new User(username, password);
        registrationService.registerUser(user);
        performAuthentication(username, password, request, response);
        userDto.setUsername(user.getUsername());

        return ResponseEntity.status(HttpStatus.CREATED).body(userDto);
    }

    @PostMapping("/sign-in")
    public ResponseEntity<UserDto> performSignIn(@RequestParam String username, @RequestParam String password, UserDto userDto,
                                                 HttpServletRequest request, HttpServletResponse response) {
        performAuthentication(username, password, request, response);
        userDto.setUsername(username);

        return ResponseEntity.status(HttpStatus.OK).body(userDto);
    }

    @PostMapping("/sign-out")
    public ResponseEntity<Void> performLogout(Authentication authentication, HttpServletRequest request, HttpServletResponse response) {
        logoutHandler.logout(request, response, authentication);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    private void performAuthentication(String username, String password, HttpServletRequest request, HttpServletResponse response) {
        Authentication userToken = new UsernamePasswordAuthenticationToken(username, password);
        Authentication authenticatedUser = authenticationManager.authenticate(userToken);

        SecurityContext context = securityContextHolderStrategy.createEmptyContext();
        context.setAuthentication(authenticatedUser);
        securityContextHolderStrategy.setContext(context);
        securityContextRepository.saveContext(context, request, response);
    }

}
