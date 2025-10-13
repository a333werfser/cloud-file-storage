package edu.example.project.service;

import edu.example.project.config.BucketProperties;
import edu.example.project.dto.ResourceDto;
import edu.example.project.exception.ResourceNotFoundException;
import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ResourceService {

    private final MinioClient minioClient;

    private final BucketProperties bucketProperties;

    private final FileService fileService;

    private final FolderService folderService;

    public ResourceDto getResourceInfo(String path, int userId) throws ResourceNotFoundException {
        String userContextPath = redirectToUserRootFolder(path, userId);
        StatObjectResponse metaobject;
        if (getResourceType(userContextPath) == ResourceType.FILE) {
            try {
                metaobject = getStatObject(userContextPath);
            } catch (ResourceNotFoundException exception) {
                throw new ResourceNotFoundException("File does not exist", exception);
            }
            return fileService.mapFileToDto(userContextPath, metaobject);
        }
        else {
            try {
                getStatObject(userContextPath);
            } catch (ResourceNotFoundException exception) {
                if (folderService.virtualFolderExists(userContextPath)) {
                    return folderService.mapFolderToDto(userContextPath);
                }
                else {
                    throw new ResourceNotFoundException("Directory does not exist", exception);
                }
            }
            return folderService.mapFolderToDto(userContextPath);
        }
    }

    public void removeResource(String path, int userId) {

    }

    private StatObjectResponse getStatObject(String path) throws ResourceNotFoundException {
        try {
            return minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucketProperties.getDefaultName())
                            .object(path)
                            .build()
            );
        } catch (Exception exception) {
            if (exception instanceof ErrorResponseException && exception.getMessage().equals("Object does not exist")) {
                throw new ResourceNotFoundException(exception.getMessage(), exception);
            }
            throw new RuntimeException(exception);
        }
    }

    private String redirectToUserRootFolder(String path, int userId) {
        String userFolder = String.format("user-%d-files/", userId);
        try {
            getStatObject(userFolder);
        } catch (ResourceNotFoundException exception) {
            folderService.createFolder(userFolder);
        }
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
