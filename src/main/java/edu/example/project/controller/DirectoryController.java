package edu.example.project.controller;

import edu.example.project.dto.PathRequest;
import edu.example.project.dto.ResourceDto;
import edu.example.project.exception.ResourceAlreadyExistsException;
import edu.example.project.exception.ResourceNotFoundException;
import edu.example.project.security.UserDetailsImpl;
import edu.example.project.service.ResourceService;
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

    @GetMapping
    public ResponseEntity<List<ResourceDto>> getFolderContents(@AuthenticationPrincipal UserDetailsImpl principle,
                                                               @Valid @ModelAttribute PathRequest pathRequest
    ) throws ResourceNotFoundException {
        List<ResourceDto> resources = resourceService.getFolderContents(principle.getId(), pathRequest.getPath());
        return ResponseEntity.status(HttpStatus.OK).body(resources);
    }

    @PostMapping
    public ResponseEntity<ResourceDto> createFolder(@AuthenticationPrincipal UserDetailsImpl principle,
                                                    @Valid @ModelAttribute PathRequest pathRequest
    ) throws ResourceAlreadyExistsException, ResourceNotFoundException {
        ResourceDto resourceDto = resourceService.createFolder(principle.getId(), pathRequest.getPath());
        return ResponseEntity.status(HttpStatus.CREATED).body(resourceDto);
    }

}
