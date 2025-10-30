package edu.example.project.controller;

import edu.example.project.dto.PathRequest;
import edu.example.project.dto.ResourceDto;
import edu.example.project.exception.ResourceAlreadyExistsException;
import edu.example.project.exception.ResourceNotFoundException;
import edu.example.project.security.UserDetailsImpl;
import edu.example.project.service.ResourceService;
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

    @GetMapping("/download")
    public ResponseEntity<Resource> downloadResource(@AuthenticationPrincipal UserDetailsImpl principle,
                                                     @Valid @ModelAttribute PathRequest pathRequest
    ) throws IOException, ResourceNotFoundException {
        byte[] resourceBinaryContent = resourceService.getResourceBinaryContent(principle.getId(), pathRequest.getPath());
        String resourceName = resourceService.resolveDownloadedResourceName(pathRequest.getPath());
        return ResponseEntity.status(HttpStatus.OK)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resourceName + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM).body(new ByteArrayResource(resourceBinaryContent));
    }

    @GetMapping("/search")
    public ResponseEntity<List<ResourceDto>> searchResource(@AuthenticationPrincipal UserDetailsImpl principle,
                                                            @RequestParam("query") String query) {
        List<ResourceDto> resources = resourceService.getResourcesInfo(principle.getId(), query);
        return ResponseEntity.status(HttpStatus.OK).body(resources);
    }

    @GetMapping("/move")
    public ResponseEntity<ResourceDto> moveResource(@AuthenticationPrincipal UserDetailsImpl principle,
                                                    @Validated(PathRequest.Copy.class) @ModelAttribute PathRequest pathRequest
    ) throws ResourceNotFoundException, ResourceAlreadyExistsException {
        ResourceDto resourceDto = resourceService.moveResource(principle.getId(), pathRequest.getFrom(), pathRequest.getTo());
        return ResponseEntity.status(HttpStatus.OK).body(resourceDto);
    }

    @PostMapping
    public ResponseEntity<List<ResourceDto>> uploadResources(@AuthenticationPrincipal UserDetailsImpl principle,
                                                             @Valid @ModelAttribute PathRequest pathRequest, @RequestParam("object") List<MultipartFile> files
    ) throws ResourceAlreadyExistsException, ResourceNotFoundException {
        List<ResourceDto> resources = resourceService.uploadResources(principle.getId(), pathRequest.getPath(), files);
        return ResponseEntity.status(HttpStatus.CREATED).body(resources);
    }

    @GetMapping
    public ResponseEntity<ResourceDto> getResourceInfo(@AuthenticationPrincipal UserDetailsImpl principle,
                                                       @Valid @ModelAttribute PathRequest pathRequest
    ) throws ResourceNotFoundException {
        ResourceDto resourceDto = resourceService.getResourceInfo(principle.getId(), pathRequest.getPath());
        return ResponseEntity.status(HttpStatus.OK).body(resourceDto);
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteResource(@AuthenticationPrincipal UserDetailsImpl principle,
                                               @Valid @ModelAttribute PathRequest pathRequest
    ) throws ResourceNotFoundException {
        resourceService.removeResource(principle.getId(), pathRequest.getPath());
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

}
