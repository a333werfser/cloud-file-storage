package edu.example.project.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResourceDto {

    private String path;

    private String name;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long size;

    private String type;

}
