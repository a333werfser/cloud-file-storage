package edu.example.project.service;

import edu.example.project.dto.ResourceDto;
import edu.example.project.exception.ResourceNotFoundException;
import io.minio.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.BufferedInputStream;
import java.io.IOException;

@Service
@RequiredArgsConstructor
public class ResourceService {

    private final MinioService minioService;

    private final FileService fileService;

    private final FolderService folderService;

    public ResourceDto getResourceInfo(String path, int userId) throws ResourceNotFoundException {
        String userContextPath = redirectToUserRootFolder(path, userId);
        StatObjectResponse statObject = findResourceInfo(userContextPath);
        if (getResourceType(userContextPath) == ResourceType.FILE) {
            return fileService.mapFileToDto(userContextPath, statObject.size());
        }
        else {
            return folderService.mapFolderToDto(userContextPath);
        }
    }

    public void removeResource(String path, int userId) throws ResourceNotFoundException {
        String userContextPath = redirectToUserRootFolder(path, userId);
        findResourceInfo(userContextPath);
        if (getResourceType(userContextPath) == ResourceType.FILE) {
            minioService.removeObject(userContextPath);
        }
        else {
            if (folderService.pathHasObjectsInside(userContextPath)) {
                minioService.removeObjectsFrom(userContextPath);
                minioService.removeObject(userContextPath);
            }
            else {
                minioService.removeObject(userContextPath);
            }
        }
    }

    private StatObjectResponse findResourceInfo(String path) throws ResourceNotFoundException {
        try {
            return minioService.statObject(path);
        } catch (ResourceNotFoundException exception) {
            if (getResourceType(path) == ResourceType.FILE) {
                throw new ResourceNotFoundException("File does not exist", exception);
            }
            else {
                if (folderService.pathHasObjectsInside(path)) {
                    folderService.createFolder(path);
                    return findResourceInfo(path);
                }
                else {
                    throw new ResourceNotFoundException("Directory does not exist", exception);
                }
            }
        }
    }

    private String redirectToUserRootFolder(String path, int userId) {
        String userFolder = String.format("user-%d-files/", userId);
        folderService.createFolder(userFolder);
        return userFolder + path;
    }

    private ResourceType getResourceType(String path) {
        if (path.endsWith("/")) {
            return ResourceType.FOLDER;
        }
        else {
            return ResourceType.FILE;
        }
    }

    private enum ResourceType {
        FOLDER,
        FILE
    }

}
