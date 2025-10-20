package edu.example.project.controller;

import edu.example.project.dto.ResourceDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/resource")
public class ResourceController {


    /**
     * filename возможно будет содержать верхнюю папку (testfold/file.txt)
     * + надо загружать сразу несколько файлов
     */
    @PostMapping
    public ResponseEntity<ResourceDto> upload(@RequestParam("path") String path, @RequestParam("file") List<MultipartFile> files) {
        return null;
    }

}
