package edu.example.project.controller;

import edu.example.project.dto.AuthRequestDto;
import edu.example.project.dto.ResponseMessageDto;
import edu.example.project.dto.UserDto;
import edu.example.project.exception.NotUniqueUsernameException;
import edu.example.project.model.User;
import edu.example.project.service.RegistrationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final SecurityContextHolderStrategy securityContextHolderStrategy = SecurityContextHolder.getContextHolderStrategy();

    private final SecurityContextRepository securityContextRepository;

    private final AuthenticationManager authenticationManager;

    private final LogoutHandler logoutHandler;

    private final RegistrationService registrationService;

    @PostMapping("/sign-up")
    public ResponseEntity<UserDto> performSignUp(@Valid @RequestBody AuthRequestDto userRequest, UserDto userDto,
                                                 HttpServletRequest request, HttpServletResponse response) {
        User user = new User(userRequest.getUsername(), userRequest.getPassword());
        registrationService.registerUser(user);
        performAuthentication(userRequest.getUsername(), userRequest.getPassword(), request, response);
        userDto.setUsername(user.getUsername());

        return ResponseEntity.status(HttpStatus.CREATED).body(userDto);
    }

    @PostMapping("/sign-in")
    public ResponseEntity<UserDto> performSignIn(@Valid @RequestBody AuthRequestDto userRequest, UserDto userDto,
                                                 HttpServletRequest request, HttpServletResponse response) {
        performAuthentication(userRequest.getUsername(), userRequest.getPassword(), request, response);
        userDto.setUsername(userRequest.getUsername());

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

    @ExceptionHandler
    public ResponseEntity<ResponseMessageDto> handle(MethodArgumentNotValidException ex) {
        List<String> constraintViolations = new ArrayList<>();
        ex.getBindingResult().getFieldErrors().forEach((fieldError) -> {
            constraintViolations.add(fieldError.getDefaultMessage());
        });
        ResponseMessageDto responseMessageDto =  new ResponseMessageDto();
        responseMessageDto.setMessage("Invalid username or password");
        responseMessageDto.setViolations(constraintViolations);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseMessageDto);
    }

    @ExceptionHandler
    public ResponseEntity<ResponseMessageDto> handle(NotUniqueUsernameException ex) {
        ResponseMessageDto responseMessageDto =  new ResponseMessageDto();
        responseMessageDto.setMessage(ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(responseMessageDto);
    }

}
