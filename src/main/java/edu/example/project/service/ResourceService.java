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

    public byte[] getResourceBinaryContent(String path, int userId) throws ResourceNotFoundException, IOException {
        StatObjectResponse statObject = findResourceInfo(redirectToUserRootFolder(path, userId));
        if (getResourceType(statObject.object()) == ResourceType.FILE) {
            return fileService.getFileBinaryContent(statObject.object());
        }
        else {
            return folderService.getFolderBinaryContentZipped(statObject.object());
        }
    }

    public ResourceDto getResourceInfo(String path, int userId) throws ResourceNotFoundException {
        StatObjectResponse statObject = findResourceInfo(redirectToUserRootFolder(path, userId));
        if (getResourceType(statObject.object()) == ResourceType.FILE) {
            return fileService.mapFileToDto(statObject.object(), statObject.size());
        }
        else {
            return folderService.mapFolderToDto(statObject.object());
        }
    }

    public void removeResource(String path, int userId) throws ResourceNotFoundException {
        StatObjectResponse statObject = findResourceInfo(redirectToUserRootFolder(path, userId));
        if (getResourceType(statObject.object()) == ResourceType.FILE) {
            minioService.removeObject(statObject.object());
        }
        else {
            if (folderService.pathHasObjectsInside(statObject.object())) {
                minioService.removeObjectsFrom(statObject.object());
                minioService.removeObject(statObject.object());
            }
            else {
                minioService.removeObject(statObject.object());
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
