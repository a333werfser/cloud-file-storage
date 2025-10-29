package edu.example.project.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ResponseMessage {

    private String message;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<String> violations;

    public ResponseMessage(String message) {
        this.message = message;
    }

}
