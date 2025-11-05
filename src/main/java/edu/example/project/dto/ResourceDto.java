package edu.example.project.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
public class ResourceDto {

    @Schema(description = "path to parent folder", example = "path/to/")
    private String path;

    @Schema(description = "resource name", example = "file.txt")
    private String name;

    @Schema(description = "resource size (not required if resource is folder)", example = "123")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long size;

    @Schema(description = "resource type", example = "FILE")
    private String type;

}
