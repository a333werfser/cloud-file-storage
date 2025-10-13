package edu.example.project.service;

import edu.example.project.config.BucketProperties;
import edu.example.project.dto.ResourceDto;
import edu.example.project.exception.ResourceNotFoundException;
import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ResourceService {

    private final MinioClient minioClient;

    private final BucketProperties bucketProperties;

    private final FileService fileService;

    private final FolderService folderService;

    public ResourceDto getResourceInfo(String path, int userId) throws ResourceNotFoundException {
        String userContextPath = redirectToUserRootFolder(path, userId);
        StatObjectResponse statObject = getStatObject(userContextPath);

        if (getResourceType(userContextPath) == ResourceType.FILE) {
            return fileService.mapFileToDto(userContextPath, statObject);
        }
        else {
            return folderService.mapFolderToDto(userContextPath);
        }
    }

    public void removeResource(String path, int userId) throws ResourceNotFoundException {
        String userContextPath = redirectToUserRootFolder(path, userId);
        StatObjectResponse statObject = getStatObject(userContextPath);
        if (getResourceType(userContextPath) == ResourceType.FILE) {
            removeObject(userContextPath);
        }
        else {
            if (getObjectsList(userContextPath).iterator().hasNext()) {
                removeObjects(userContextPath);
                removeObject(userContextPath);
            }
            else {
                removeObject(userContextPath);
            }
        }
    }

    public void removeObject(String path) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketProperties.getDefaultName())
                            .object(path)
                            .build()
            );
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    public void removeObjects(String path) {
        List<DeleteObject> deleteObjects = new ArrayList<>();
        getObjectsList(path).forEach(
                object -> {
                    try {
                        deleteObjects.add(new DeleteObject(object.get().objectName()));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
        Iterable<Result<DeleteError>> results = minioClient.removeObjects(
                RemoveObjectsArgs.builder()
                        .bucket(bucketProperties.getDefaultName())
                        .objects(deleteObjects)
                        .build()
        );
        for (Result<DeleteError> result : results) {
            try {
                result.get();
            } catch(Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public Iterable<Result<Item>> getObjectsList(String prefix) {
        return minioClient.listObjects(
               ListObjectsArgs.builder()
                       .bucket(bucketProperties.getDefaultName())
                       .prefix(prefix)
                       .recursive(true)
                       .build()
        );
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
                if (getResourceType(path)  == ResourceType.FILE) {
                    throw new ResourceNotFoundException("File does not exist", exception);
                }
                else {
                    if (folderService.virtualFolderExists(path)) {
                        folderService.createFolder(path);
                        return getStatObject(path);
                    }
                    else {
                        throw new ResourceNotFoundException("Directory does not exist", exception);
                    }
                }
            }
            throw new RuntimeException(exception);
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
