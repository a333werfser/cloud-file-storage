package edu.example.project.controller;

import edu.example.project.dto.AuthRequest;
import edu.example.project.dto.ResponseMessage;
import edu.example.project.dto.UserDto;
import edu.example.project.exception.AuthenticationFailedException;
import edu.example.project.exception.NotUniqueUsernameException;
import edu.example.project.model.User;
import edu.example.project.service.RegistrationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<UserDto> performSignUp(@Valid @RequestBody AuthRequest userRequest, UserDto userDto,
                                                 HttpServletRequest request, HttpServletResponse response) {
        User user = new User(userRequest.getUsername(), userRequest.getPassword());
        registrationService.registerUser(user);
        performAuthentication(userRequest.getUsername(), userRequest.getPassword(), request, response);
        userDto.setUsername(user.getUsername());

        return ResponseEntity.status(HttpStatus.CREATED).body(userDto);
    }

    @PostMapping("/sign-in")
    public ResponseEntity<UserDto> performSignIn(@Valid @RequestBody AuthRequest userRequest, UserDto userDto,
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
//        try {
            Authentication userToken = new UsernamePasswordAuthenticationToken(username, password);
            Authentication authenticatedUser = authenticationManager.authenticate(userToken);
            SecurityContext context = securityContextHolderStrategy.createEmptyContext();
            context.setAuthentication(authenticatedUser);
            securityContextHolderStrategy.setContext(context);
            securityContextRepository.saveContext(context, request, response);
//        } catch (AuthenticationException e) {
//            if (e instanceof BadCredentialsException) {
//                throw new AuthenticationFailedException("User not found");
//            }
//            throw e;
//        }
    }

    @ExceptionHandler
    public ResponseEntity<ResponseMessage> handle(MethodArgumentNotValidException ex) {
        List<String> constraintViolations = new ArrayList<>();
        for (FieldError fieldError : ex.getFieldErrors()) {
            constraintViolations.add(fieldError.getDefaultMessage());
        }
        ResponseMessage responseMessageDto =  new ResponseMessage();
        responseMessageDto.setMessage("Invalid username or password");
        responseMessageDto.setViolations(constraintViolations);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseMessageDto);
    }

//    @ExceptionHandler
//    public ResponseEntity<ResponseMessage> handle(AuthenticationFailedException ex) {
//        ResponseMessage responseMessageDto = new ResponseMessage();
//        responseMessageDto.setMessage(ex.getMessage());
//        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseMessageDto);
//    }

    @ExceptionHandler
    public ResponseEntity<ResponseMessage> handle(NotUniqueUsernameException ex) {
        ResponseMessage responseMessageDto =  new ResponseMessage();
        responseMessageDto.setMessage(ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(responseMessageDto);
    }

}
