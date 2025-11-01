package edu.example.project.controller;

import edu.example.project.dto.ResponseMessage;
import edu.example.project.dto.UserDto;
import edu.example.project.security.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/user")
@RestController
public class UserController {

    @Operation(
            summary = "Get current user",
            description = "### Return username of current authenticated user",
            security = @SecurityRequirement(name = "Session-based")
    )
    @ApiResponse(
            responseCode = "200",
            description = "Success",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = UserDto.class)
            )
    )
    @ApiResponse(
            responseCode = "401",
            description = "Unauthorized",
            content = @Content()
    )
    @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ResponseMessage.class)
            )
    )
    @GetMapping("/me")
    public ResponseEntity<UserDto> getCurrentUser(@AuthenticationPrincipal UserDetailsImpl principal) {
        UserDto userDto = new UserDto();
        userDto.setUsername(principal.getUsername());
        return ResponseEntity.ok().body(userDto);
    }

}
