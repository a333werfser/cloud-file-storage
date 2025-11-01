package edu.example.project.controller;

import edu.example.project.dto.AuthRequest;
import edu.example.project.dto.ResponseMessage;
import edu.example.project.dto.UserDto;
import edu.example.project.exception.AuthenticationFailedException;
import edu.example.project.exception.NotUniqueUsernameException;
import edu.example.project.model.User;
import edu.example.project.service.RegistrationService;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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

    @Operation(
            summary = "Register user",
            description = "### Register a new user and immediately authenticate him",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "### User credentials")
    )
    @ApiResponse(
            responseCode = "201",
            description = "Successful registration",
            headers = {
                    @Header(
                            name = "Set-Cookie",
                            description = "Session cookie (Spring Security creates JSESSIONID automatically)"
                    )
            },
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = UserDto.class)
            )
    )
    @ApiResponse(
            responseCode = "400",
            description = "Validation errors",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ResponseMessage.class)
            )
    )
    @ApiResponse(
            responseCode = "409",
            description = "Username already exists",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ResponseMessage.class)
            )
    )
    @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ResponseMessage.class)
            )
    )
    @PostMapping("/sign-up")
    public ResponseEntity<UserDto> performSignUp(@Valid @RequestBody AuthRequest userRequest,
                                                 HttpServletRequest request, HttpServletResponse response) {
        UserDto userDto = new UserDto();
        User user = new User(userRequest.getUsername(), userRequest.getPassword());
        registrationService.registerUser(user);
        performAuthentication(userRequest.getUsername(), userRequest.getPassword(), request, response);
        userDto.setUsername(user.getUsername());

        return ResponseEntity.status(HttpStatus.CREATED).body(userDto);
    }


    @Operation(
            summary = "Authenticate user",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "### User credentials")
    )
    @ApiResponse(
            responseCode = "200",
            description = "Successful authentication",
            headers = {
                    @Header(
                            name = "Set-Cookie",
                            description = "Session cookie (Spring Security creates JSESSIONID automatically)"
                    )
            },
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = UserDto.class)
            )
    )
    @ApiResponse(
            responseCode = "400",
            description = "Validation errors",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ResponseMessage.class)
            )
    )
    @ApiResponse(
            responseCode = "401",
            description = "User not found",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ResponseMessage.class)
            )
    )
    @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ResponseMessage.class)
            )
    )
    @PostMapping("/sign-in")
    public ResponseEntity<UserDto> performSignIn(@Valid @RequestBody AuthRequest userRequest,
                                                 HttpServletRequest request, HttpServletResponse response) {
        UserDto userDto = new UserDto();
        performAuthentication(userRequest.getUsername(), userRequest.getPassword(), request, response);
        userDto.setUsername(userRequest.getUsername());

        return ResponseEntity.status(HttpStatus.OK).body(userDto);
    }

    @Operation(
            summary = "Log out authenticated user",
            security = @SecurityRequirement(name = "Session-based")
    )
    @ApiResponse(
            responseCode = "204",
            description = "Successful logout",
            headers = {
                    @Header(
                            name = "Set-Cookie",
                            description = "Invalidate Session cookie"
                    )
            }
    )
    @ApiResponse(
            responseCode = "401",
            description = "Unauthorized"
    )
    @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ResponseMessage.class)
            )
    )
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
    public ResponseEntity<ResponseMessage> handle(MethodArgumentNotValidException ex) {
        List<String> constraintViolations = new ArrayList<>();
        for (FieldError fieldError : ex.getFieldErrors()) {
            constraintViolations.add(fieldError.getDefaultMessage());
        }
        ResponseMessage message =  new ResponseMessage();
        message.setMessage("Invalid username or password");
        message.setViolations(constraintViolations);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(message);
    }

    @ExceptionHandler
    public ResponseEntity<ResponseMessage> handle(NotUniqueUsernameException ex) {
        ResponseMessage message =  new ResponseMessage();
        message.setMessage(ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(message);
    }

}
