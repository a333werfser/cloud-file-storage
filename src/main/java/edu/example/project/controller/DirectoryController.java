package edu.example.project.controller;

import edu.example.project.dto.PathRequest;
import edu.example.project.dto.ResourceDto;
import edu.example.project.dto.ResponseMessage;
import edu.example.project.exception.ResourceAlreadyExistsException;
import edu.example.project.exception.ResourceNotFoundException;
import edu.example.project.security.UserDetailsImpl;
import edu.example.project.service.ResourceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/directory")
@RequiredArgsConstructor
public class DirectoryController {

    private final ResourceService resourceService;

    @Operation(
            summary = "Get folder content",
            description = "### Return not recursive list of folder content",
            security = @SecurityRequirement(name = "Session-based"),
            parameters = {
                    @Parameter(
                            name = "path",
                            in = ParameterIn.QUERY,
                            description = "Absolute path to folder (slash-separated)",
                            example = "path/to/folder/",
                            required = true
                    )
            }
    )
    @ApiResponse(
            responseCode = "200",
            description = "Successfully found folder content",
            content = @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = ResourceDto.class))
            )
    )
    @ApiResponse(
            responseCode = "400",
            description = "Invalid path",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ResponseMessage.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "Folder does not exist",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ResponseMessage.class)
            )
    )
    @ApiResponse(
            responseCode = "401",
            description = "Unauthorized",
            content = @Content()
    )
    @GetMapping
    public ResponseEntity<List<ResourceDto>> getFolderContents(@AuthenticationPrincipal UserDetailsImpl principle,
                                                               @Valid @ModelAttribute @Parameter(hidden = true) PathRequest pathRequest
    ) throws ResourceNotFoundException {
        List<ResourceDto> resources = resourceService.getFolderContents(principle.getId(), pathRequest.getPath());
        return ResponseEntity.status(HttpStatus.OK).body(resources);
    }

    @Operation(
            summary = "Create folder",
            description = "### Create folder",
            security = @SecurityRequirement(name = "Session-based"),
            parameters = {
                    @Parameter(
                            name = "path",
                            in = ParameterIn.QUERY,
                            description = "Absolute path to folder (slash-separated)",
                            example = "path/to/folder/",
                            required = true
                    )
            }
    )
    @ApiResponse(
            responseCode = "201",
            description = "Successfully created folder",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ResourceDto.class)
            )
    )
    @ApiResponse(
            responseCode = "400",
            description = "Invalid path",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ResponseMessage.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "Parent folder does not exist",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ResponseMessage.class)
            )
    )
    @ApiResponse(
            responseCode = "409",
            description = "Folder already exists",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ResponseMessage.class)
            )
    )
    @ApiResponse(
            responseCode = "401",
            description = "Unauthorized",
            content = @Content()
    )
    @PostMapping
    public ResponseEntity<ResourceDto> createFolder(@AuthenticationPrincipal UserDetailsImpl principle,
                                                    @Valid @ModelAttribute @Parameter(hidden = true) PathRequest pathRequest
    ) throws ResourceAlreadyExistsException, ResourceNotFoundException {
        ResourceDto resourceDto = resourceService.createFolder(principle.getId(), pathRequest.getPath());
        return ResponseEntity.status(HttpStatus.CREATED).body(resourceDto);
    }

}
