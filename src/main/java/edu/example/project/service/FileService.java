package edu.example.project.service;

import edu.example.project.dto.ResourceDto;
import edu.example.project.exception.BadResourceTypeException;
import edu.example.project.exception.ResourceAlreadyExistsException;
import edu.example.project.exception.ResourceNotFoundException;
import io.minio.StatObjectResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@RequiredArgsConstructor
public class FileService implements PathResolverService {

    private final MinioService minioService;

    protected void moveFile(String from, String to) {
        if (from.equals(to)) {
            return;
        }
        minioService.copyObject(from, to);
        try {
            minioService.statObject(to);
        } catch (ResourceNotFoundException exception) {
            throw new RuntimeException("Copy failed", exception);
        }
        minioService.removeObject(from);
    }

    protected byte[] getFileBinaryContent(String path) throws IOException {
        ensureFilePath(path);
        try (InputStream objectIn = minioService.getObject(path)) {
            return objectIn.readAllBytes();
        }
    }

    protected ResourceDto mapFileToDto(String path, Long size) {
        ensureFilePath(path);
        ResourceDto resourceDto = new ResourceDto();
        path = eraseUserRootFolder(path);
        resourceDto.setPath(resolvePathToFile(path));
        resourceDto.setName(resolveFileName(path));
        resourceDto.setSize(size);
        resourceDto.setType("FILE");
        return resourceDto;
    }

    private String resolveFileName(String path) {
        return Paths.get(path).getFileName().toString();
    }

    private String resolvePathToFile(String path) {
        if (!path.contains("/")) {
            return "";
        }
        else {
            return path.substring(0, path.lastIndexOf("/") + 1);
        }
    }

    private void ensureFilePath(String path) {
        if (path.endsWith("/")) {
            throw new BadResourceTypeException("Path to file expected");
        }
    }

}
