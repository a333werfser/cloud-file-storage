package edu.example.project.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ResponseMessage {

    @Schema(description = "error message", example = "error_message", requiredMode = Schema.RequiredMode.REQUIRED)
    private String message;

    @ArraySchema(
            schema = @Schema(description = "Optional field, sends only if constraint was violated", example = "Username must follow [A-Za-z0-9_] pattern")
    )
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<String> violations;

    public ResponseMessage(String message) {
        this.message = message;
    }

}
