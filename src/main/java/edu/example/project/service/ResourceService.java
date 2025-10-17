package edu.example.project.service;

import edu.example.project.dto.ResourceDto;
import edu.example.project.exception.BadResourceTypeException;
import edu.example.project.exception.ResourceAlreadyExistsException;
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

    /**
     *
     * @param from ex. root/folder/item (if move)   ex. root/folder/item (if rename)
     * @param to   ex. root/directory/item (if move)    ex. root/folder/not-item (if rename)
     * @return
     */
    public ResourceDto moveResource(int userId, String from, String to) throws ResourceNotFoundException, ResourceAlreadyExistsException {
        StatObjectResponse statObject = findResourceInfo(redirectToUserRootFolder(userId, from));
        try {
            findResourceInfo(to);
        } catch (ResourceNotFoundException exception) {
            from = statObject.object();
            to = redirectToUserRootFolder(userId, to);
            if (getResourceType(from) != getResourceType(to)) {
                throw new BadResourceTypeException("Can't convert File to Directory and vice versa");
            }
            if (getResourceType(from) == ResourceType.FILE) {
                fileService.moveFile(from, to);
                StatObjectResponse moved = findResourceInfo(to);
                return fileService.mapFileToDto(moved.object(), moved.size());
            }
            else {
                folderService.moveFolder(from, to);
                StatObjectResponse moved = findResourceInfo(to);
                return folderService.mapFolderToDto(moved.object());
            }
        }
        throw new ResourceAlreadyExistsException("Resource along the path already exists");
    }

    public byte[] getResourceBinaryContent(int userId, String path) throws ResourceNotFoundException, IOException {
        StatObjectResponse statObject = findResourceInfo(redirectToUserRootFolder(userId, path));
        if (getResourceType(statObject.object()) == ResourceType.FILE) {
            return fileService.getFileBinaryContent(statObject.object());
        }
        else {
            return folderService.getFolderBinaryContentZipped(statObject.object());
        }
    }

    public ResourceDto getResourceInfo(int userId, String path) throws ResourceNotFoundException {
        StatObjectResponse statObject = findResourceInfo(redirectToUserRootFolder(userId, path));
        if (getResourceType(statObject.object()) == ResourceType.FILE) {
            return fileService.mapFileToDto(statObject.object(), statObject.size());
        }
        else {
            return folderService.mapFolderToDto(statObject.object());
        }
    }

    public void removeResource(int userId, String path) throws ResourceNotFoundException {
        StatObjectResponse statObject = findResourceInfo(redirectToUserRootFolder(userId, path));
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

    private String redirectToUserRootFolder(int userId, String path) {
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
