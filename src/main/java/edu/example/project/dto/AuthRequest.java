package edu.example.project.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuthRequest {

    private static final int MAXIMUM_PASSWORD_LENGTH = 20;

    private static final int MAXIMUM_USERNAME_LENGTH = 20;

    private static final int MINIMUM_PASSWORD_LENGTH = 8;

    private static final int MINIMUM_USERNAME_LENGTH = 4;

    private static final String USERNAME_NULL = "Username must not be null";

    private static final String PASSWORD_NULL = "Password must not be null";

    private static final String USERNAME_INVALID_SIZE = "Username length must be between 4 and 20 characters";

    private static final String PASSWORD_INVALID_SIZE = "Password length must be between 8 and 20 characters";

    private static final String USERNAME_INVALID_PATTERN = "Username must follow [A-Za-z0-9_] pattern";

    private static final String PASSWORD_INVALID_PATTERN = "Password must not contain whitespaces";

    /**
     * Username must contain only latin characters, 0-9 digits and _. It is also case-insensitive
     */
    @Schema(description = "username", example = "john_doe")
    @NotNull(message = USERNAME_NULL)
    @Pattern(regexp = "^[A-Za-z0-9_]+$", message = USERNAME_INVALID_PATTERN)
    @Size(min = MINIMUM_USERNAME_LENGTH, max = MAXIMUM_USERNAME_LENGTH, message = USERNAME_INVALID_SIZE)
    private String username;

    /**
     * Password must not contain whitespaces
     */
    @Schema(description = "password", example = "s0me_$trong_p@ss")
    @NotNull(message = PASSWORD_NULL)
    @Pattern(regexp = "\\S+", message = PASSWORD_INVALID_PATTERN)
    @Size(min = MINIMUM_PASSWORD_LENGTH, max = MAXIMUM_PASSWORD_LENGTH, message = PASSWORD_INVALID_SIZE)
    private String password;

}
