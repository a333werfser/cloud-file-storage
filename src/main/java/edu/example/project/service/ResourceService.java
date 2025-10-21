package edu.example.project.service;

import edu.example.project.dto.ResourceDto;
import edu.example.project.exception.BadResourceTypeException;
import edu.example.project.exception.ResourceAlreadyExistsException;
import edu.example.project.exception.ResourceNotFoundException;
import io.minio.*;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ResourceService {

    private final MinioService minioService;

    private final FileService fileService;

    private final FolderService folderService;

    public ResourceDto createFolder(int userId, String path) {
        folderService.ensureFolderPath(path);
        String userContextPath = redirectToUserRootFolder(userId, path);
        folderService.createFolder(userContextPath);
        createNecessaryFolders(userContextPath);
        return folderService.mapFolderToDto(userContextPath);
    }

    public List<ResourceDto> getFolderContents(int userId, String path) throws ResourceNotFoundException {
        folderService.ensureFolderPath(path);
        StatObjectResponse resourceInfo = findResourceInfo(redirectToUserRootFolder(userId, path));
        if (folderService.pathHasObjectsInside(resourceInfo.object())) {
            return findResourcesBy(resourceInfo.object(), false);
        }
        else {
            return new ArrayList<>();
        }
    }

    public List<ResourceDto> uploadResources(int userId, String path, List<MultipartFile> files) throws IOException, ResourceNotFoundException, ResourceAlreadyExistsException {
        folderService.ensureFolderPath(path);
        String userContextPath = redirectToUserRootFolder(userId, path);
        if (filesNotExist(userContextPath, files)) {
            ArrayList<ResourceDto> resources = new ArrayList<>();
            for (MultipartFile file : files) {
                String objectKey = userContextPath + file.getOriginalFilename();
                minioService.putObject(objectKey, file);
                StatObjectResponse written = findResourceInfo(objectKey);
                createNecessaryFolders(objectKey);
                resources.add(fileService.mapFileToDto(written.object(), written.size()));
            }
            return resources;
        }
        else {
            throw new ResourceAlreadyExistsException("Resource along the path already exists");
        }
    }

    private boolean filesNotExist(String userContextPath, List<MultipartFile> files) {
        for (MultipartFile file : files) {
            String objectKey = userContextPath + resolveRootElement(Objects.requireNonNull(file.getOriginalFilename()));
            try {
                findResourceInfo(objectKey);
                return false;
            } catch (ResourceNotFoundException ignored) {}
        }
        return true;
    }

    private String resolveRootElement(String path) {
        if (!path.contains("/")) {
            return path;
        }
        else {
            return path.substring(0, path.indexOf("/") + 1);
        }
    }

    public List<ResourceDto> getResourcesInfo(int userId, String prefix) {
        return findResourcesBy(redirectToUserRootFolder(userId, prefix), true);
    }

    private List<ResourceDto> findResourcesBy(String prefix, boolean isRecursive) {
        List<ResourceDto> resources = new ArrayList<>();
        minioService.listObjects(prefix, isRecursive).forEach((result) -> {
            try {
                Item resourceInfo = result.get();
                if (resourceInfo.isDir()) {
                    resources.add(folderService.mapFolderToDto(resourceInfo.objectName()));
                } else {
                    resources.add(fileService.mapFileToDto(resourceInfo.objectName(), resourceInfo.size()));
                }
            } catch (Exception exception) {
                throw new RuntimeException(exception);
            }
        });
        return resources;
    }

    public ResourceDto moveResource(int userId, String from, String to) throws ResourceNotFoundException, ResourceAlreadyExistsException {
        StatObjectResponse resourceInfo = findResourceInfo(redirectToUserRootFolder(userId, from));
        try {
            findResourceInfo(to);
        } catch (ResourceNotFoundException exception) {
            if (getResourceType(from) != getResourceType(to)) {
                throw new BadResourceTypeException("Can't convert File to Directory and vice versa");
            }
            from = resourceInfo.object();
            to = redirectToUserRootFolder(userId, to);
            if (getResourceType(from) == ResourceType.FILE) {
                fileService.moveFile(from, to);
                StatObjectResponse moved = findResourceInfo(to);
                createNecessaryFolders(to);
                return fileService.mapFileToDto(moved.object(), moved.size());
            }
            else {
                folderService.moveFolder(from, to);
                StatObjectResponse moved = findResourceInfo(to);
                createNecessaryFolders(to);
                return folderService.mapFolderToDto(moved.object());
            }
        }
        throw new ResourceAlreadyExistsException("Resource along the path already exists");
    }

    private void createNecessaryFolders(String path) {
        String[] names = path.split("/");
        StringBuilder folder = new StringBuilder();
        for (int i = 0; i < names.length - 1; i++) {
            String name = names[i];
            folder.append(name).append("/");
            folderService.createFolder(folder.toString());
        }
    }

    public byte[] getResourceBinaryContent(int userId, String path) throws ResourceNotFoundException, IOException {
        StatObjectResponse resourceInfo = findResourceInfo(redirectToUserRootFolder(userId, path));
        if (getResourceType(resourceInfo.object()) == ResourceType.FILE) {
            return fileService.getFileBinaryContent(resourceInfo.object());
        }
        else {
            return folderService.getFolderBinaryContentZipped(resourceInfo.object());
        }
    }

    public ResourceDto getResourceInfo(int userId, String path) throws ResourceNotFoundException {
        StatObjectResponse resourceInfo = findResourceInfo(redirectToUserRootFolder(userId, path));
        if (getResourceType(resourceInfo.object()) == ResourceType.FILE) {
            return fileService.mapFileToDto(resourceInfo.object(), resourceInfo.size());
        }
        else {
            return folderService.mapFolderToDto(resourceInfo.object());
        }
    }

    public void removeResource(int userId, String path) throws ResourceNotFoundException {
        StatObjectResponse resourceInfo = findResourceInfo(redirectToUserRootFolder(userId, path));
        if (getResourceType(resourceInfo.object()) == ResourceType.FILE) {
            minioService.removeObject(resourceInfo.object());
        }
        else {
            if (folderService.pathHasObjectsInside(resourceInfo.object())) {
                minioService.removeObjectsFrom(resourceInfo.object());
                minioService.removeObject(resourceInfo.object());
            }
            else {
                minioService.removeObject(resourceInfo.object());
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
                throw new ResourceNotFoundException("Directory does not exist", exception);
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
