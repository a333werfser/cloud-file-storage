package edu.example.project.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class UserDto {

    @Schema(description = "User which successfully made auth operation", example = "your_username")
    private String username;

}
