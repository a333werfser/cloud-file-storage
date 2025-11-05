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
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/resource")
@RequiredArgsConstructor
public class ResourceController {

    private final ResourceService resourceService;

    @Operation(
            summary = "Download resource",
            description = "### Download specified resource",
            security = @SecurityRequirement(name = "Session-based"),
            parameters = {
                    @Parameter(
                            name = "path",
                            in = ParameterIn.QUERY,
                            description = "Absolute path to resource (slash-separated)",
                            example = "path/to/file.txt",
                            required = true
                    )
            }
    )
    @ApiResponse(
            responseCode = "200",
            description = "Resource downloaded successfully",
            content = @Content(
                    mediaType = "application/octet-stream",
                    schema = @Schema(type = "string", format = "binary", example = "File")
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
            description = "Resource not found",
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
    @GetMapping("/download")
    public ResponseEntity<Resource> downloadResource(@AuthenticationPrincipal UserDetailsImpl principle,
                                                     @Valid @ModelAttribute @Parameter(hidden = true) PathRequest pathRequest
    ) throws IOException, ResourceNotFoundException {
        byte[] resourceBinaryContent = resourceService.getResourceBinaryContent(principle.getId(), pathRequest.getPath());
        String resourceName = resourceService.resolveDownloadedResourceName(pathRequest.getPath());
        return ResponseEntity.status(HttpStatus.OK)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resourceName + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM).body(new ByteArrayResource(resourceBinaryContent));
    }

    @Operation(
            summary = "Search resource",
            description = "Search a collection of resources by prefix from query",
            security = @SecurityRequirement(name = "Session-based")
    )
    @ApiResponse(
            responseCode = "200",
            description = "List of found resources",
            content = @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = ResourceDto.class))
            )
    )
    @ApiResponse(
            responseCode = "400",
            description = "Invalid or missing query",
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
    @GetMapping("/search")
    public ResponseEntity<List<ResourceDto>> searchResource(@AuthenticationPrincipal UserDetailsImpl principle,
                                                            @RequestParam("query") String query) {
        List<ResourceDto> resources = resourceService.findResourcesInfo(principle.getId(), query);
        return ResponseEntity.status(HttpStatus.OK).body(resources);
    }

    @Operation(
            summary = "Move/Rename resource",
            description = "### If you want to rename - change only resource name, if you want to move - change only resource path",
            security = @SecurityRequirement(name = "Session-based"),
            parameters = {
                    @Parameter(
                            name = "from",
                            in = ParameterIn.QUERY,
                            description = "Old absolute resource path",
                            example = "path/to/",
                            required = true
                    ),
                    @Parameter(
                            name = "to",
                            in = ParameterIn.QUERY,
                            description = "New absolute resource path",
                            example = "path/to/",
                            required = true
                    )
            }
    )
    @ApiResponse(
            responseCode = "200",
            description = "Successfully renamed or moved resource",
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
            description = "Resource not found",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ResponseMessage.class)
            )
    )
    @ApiResponse(
            responseCode = "409",
            description = "Resource already exists",
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
    @GetMapping("/move")
    public ResponseEntity<ResourceDto> moveResource(@AuthenticationPrincipal UserDetailsImpl principle,
                                                    @Validated(PathRequest.Copy.class) @ModelAttribute @Parameter(hidden = true) PathRequest pathRequest
    ) throws ResourceNotFoundException, ResourceAlreadyExistsException {
        ResourceDto resourceDto = resourceService.moveResource(principle.getId(), pathRequest.getFrom(), pathRequest.getTo());
        return ResponseEntity.status(HttpStatus.OK).body(resourceDto);
    }

    @Operation(
            summary = "Upload resource",
            description = "### Upload resource to specified directory",
            security = @SecurityRequirement(name = "Session-based"),
            parameters = {
                    @Parameter(
                            name = "path",
                            in = ParameterIn.QUERY,
                            description = "Path to directory (slash-separated) where to upload",
                            example = "path/to/",
                            required = true
                    )
            }
    )
    @ApiResponse(
            responseCode = "201",
            description = "List of uploaded resources",
            content = @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = ResourceDto.class))
            )
    )
    @ApiResponse(
            responseCode = "400",
            description = "Invalid request body",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ResponseMessage.class)
            )
    )
    @ApiResponse(
            responseCode = "409",
            description = "Resource already exists",
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
    @PostMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<List<ResourceDto>> uploadResources(@AuthenticationPrincipal UserDetailsImpl principle,
                                                             @Valid @ModelAttribute @Parameter(hidden = true) PathRequest pathRequest, @RequestParam("object") @Parameter(description = "File to upload, max size = 100mb") List<MultipartFile> files
    ) throws ResourceAlreadyExistsException, ResourceNotFoundException {
        List<ResourceDto> resources = resourceService.uploadResources(principle.getId(), pathRequest.getPath(), files);
        return ResponseEntity.status(HttpStatus.CREATED).body(resources);
    }

    @Operation(
            summary = "Get resource Info",
            description = "### Return information about target resource",
            security = @SecurityRequirement(name = "Session-based"),
            parameters = {
                    @Parameter(
                            name = "path",
                            in = ParameterIn.QUERY,
                            description = "Absolute path to target Resource (slash-separated). Path to directory must ends with /\n",
                            example = "path/to/file.txt",
                            required = true
                    )
            }
    )
    @ApiResponse(
            responseCode = "200",
            description = "Successfully found Resource",
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
            description = "Resource not found",
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
    public ResponseEntity<ResourceDto> getResourceInfo(@AuthenticationPrincipal UserDetailsImpl principle,
                                                       @Valid @ModelAttribute @Parameter(hidden = true) PathRequest pathRequest
    ) throws ResourceNotFoundException {
        ResourceDto resourceDto = resourceService.getResourceInfo(principle.getId(), pathRequest.getPath());
        return ResponseEntity.status(HttpStatus.OK).body(resourceDto);
    }

    @Operation(
            summary = "Delete resource",
            description = "### Delete target resource",
            security = @SecurityRequirement(name = "Session-based"),
            parameters = {
                    @Parameter(
                            name = "path",
                            in = ParameterIn.QUERY,
                            description = "Absolute path to target Resource (slash-separated). Path to directory must ends with /\n",
                            example = "path/to/file.txt",
                            required = true
                    )
            }
    )
    @ApiResponse(
            responseCode = "204",
            description = "Successfully delete Resource",
            content = @Content()
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
            description = "Resource not found",
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
    @DeleteMapping
    public ResponseEntity<Void> deleteResource(@AuthenticationPrincipal UserDetailsImpl principle,
                                               @Valid @ModelAttribute @Parameter(hidden = true) PathRequest pathRequest
    ) throws ResourceNotFoundException {
        resourceService.removeResource(principle.getId(), pathRequest.getPath());
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

}
