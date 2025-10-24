package edu.example.project.controller;

import edu.example.project.dto.UserDto;
import edu.example.project.security.UserDetailsImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/user")
@RestController
public class UserController {

    @GetMapping("/me")
    public ResponseEntity<UserDto> getCurrentUser(@AuthenticationPrincipal UserDetailsImpl principal) {
        UserDto userDto = new UserDto();
        userDto.setUsername(principal.getUsername());
        return ResponseEntity.ok().body(userDto);
    }

}
