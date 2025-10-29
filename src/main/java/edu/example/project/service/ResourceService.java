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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ResourceService {

    private final MinioService minioService;

    private final FileService fileService;

    private final FolderService folderService;

    public ResourceDto createFolder(Long userId, String path) throws ResourceNotFoundException, ResourceAlreadyExistsException {
        folderService.ensureFolderPath(path);
        try {
            String parentFolder = folderService.resolvePathToFolder(path);
            String userContextPath = redirectToUserRootFolder(userId, path);
            findResourceInfo(redirectToUserRootFolder(userId, parentFolder));
            try {
                findResourceInfo(userContextPath);
                throw new ResourceAlreadyExistsException("Folder already exists");
            } catch (ResourceNotFoundException exception) {
                folderService.createFolder(userContextPath);
                return folderService.mapFolderToDto(userContextPath);
            }
        } catch (ResourceNotFoundException exception) {
            throw new ResourceNotFoundException("Parent folder not exists", exception);
        }
    }

    public List<ResourceDto> getFolderContents(Long userId, String path) throws ResourceNotFoundException {
        folderService.ensureFolderPath(path);
        StatObjectResponse resourceInfo = findResourceInfo(redirectToUserRootFolder(userId, path));
        if (folderService.pathHasObjectsInside(resourceInfo.object())) {
            return findFolderContents(resourceInfo.object());
        }
        else {
            return new ArrayList<>();
        }
    }

    public List<ResourceDto> uploadResources(Long userId, String path, List<MultipartFile> files) throws ResourceNotFoundException, ResourceAlreadyExistsException {
        folderService.ensureFolderPath(path);
        try {
            StatObjectResponse folderInfo = findResourceInfo(redirectToUserRootFolder(userId, path));
            if (resourcesAlongPathNotExist(folderInfo.object(), files)) {
                ArrayList<ResourceDto> resources = new ArrayList<>();
                for (MultipartFile file : files) {
                    String objectKey = folderInfo.object() + file.getOriginalFilename();
                    minioService.putObject(objectKey, file);
                    StatObjectResponse written = findResourceInfo(objectKey);
                    createNecessaryFolders(folderInfo.object(), objectKey);
                    resources.add(fileService.mapFileToDto(written.object(), written.size()));
                }
                return resources;
            } else {
                throw new ResourceAlreadyExistsException("Resource along the path already exists");
            }
        } catch (ResourceNotFoundException exception) {
            throw new ResourceNotFoundException("Folder to upload files not found", exception);
        }
    }

    private boolean resourcesAlongPathNotExist(String userContextPath, List<MultipartFile> files) {
        for (MultipartFile file : files) {
            String objectKey = userContextPath + folderService.resolveRootElement(Objects.requireNonNull(file.getOriginalFilename()));
            try {
                findResourceInfo(objectKey);
                return false;
            } catch (ResourceNotFoundException ignored) {}
        }
        return true;
    }

    public List<ResourceDto> getResourcesInfo(Long userId, String prefix) {
        return findResources(redirectToUserRootFolder(userId, prefix));
    }

    private List<ResourceDto> findResources(String prefix) {
        List<ResourceDto> resources = new ArrayList<>();
        for (Result<Item> result : minioService.listObjects(prefix, true)) {
            try {
                Item resourceInfo = result.get();
                if (resourceInfo.objectName().endsWith("/")) {
                    resources.add(folderService.mapFolderToDto(resourceInfo.objectName()));
                } else {
                    resources.add(fileService.mapFileToDto(resourceInfo.objectName(), resourceInfo.size()));
                }
            } catch (Exception exception) {
                throw new RuntimeException(exception);
            }
        }
        return resources;
    }

    private List<ResourceDto> findFolderContents(String path) {
        List<ResourceDto> resources = new ArrayList<>();
        for (Result<Item> result : minioService.listObjects(path, false)) {
            try {
                Item resourceInfo = result.get();
                if (resourceInfo.objectName().equals(path)) {
                    continue;
                }
                if (resourceInfo.objectName().endsWith("/")) {
                    resources.add(folderService.mapFolderToDto(resourceInfo.objectName()));
                } else {
                    resources.add(fileService.mapFileToDto(resourceInfo.objectName(), resourceInfo.size()));
                }
            } catch (Exception exception) {
                throw new RuntimeException(exception);
            }
        }
        return resources;
    }

    public ResourceDto moveResource(Long userId, String from, String to) throws ResourceNotFoundException, ResourceAlreadyExistsException {
        ensureEqualResourceTypes(from, to);
        to = redirectToUserRootFolder(userId, to);
        from = redirectToUserRootFolder(userId, from);

        ensureSrcResourceExists(from);
        ensureParentFolderExists(to);
        ensureTargetResourceNotExists(to);

        if (getResourceType(from) == ResourceType.FILE) {
            fileService.moveFile(from, to);
            StatObjectResponse moved = findResourceInfo(to);
            return fileService.mapFileToDto(moved.object(), moved.size());
        } else {
            folderService.moveFolder(from, to);
            StatObjectResponse moved = findResourceInfo(to);
            return folderService.mapFolderToDto(moved.object());
        }
    }

    private void ensureEqualResourceTypes(String from, String to) {
        if (getResourceType(from) != getResourceType(to)) {
            throw new BadResourceTypeException("Can't convert File to Directory and vice versa");
        }
    }

    private void ensureParentFolderExists(String to) throws ResourceNotFoundException {
        try {
            findResourceInfo(resolveParentFolder(to));
        } catch (ResourceNotFoundException exception) {
            throw new ResourceNotFoundException("Parent folder not exists", exception);
        }
    }

    private String resolveParentFolder(String path) {
        if (getResourceType(path) == ResourceType.FOLDER) {
            return folderService.resolvePathToFolder(path);
        }
        else {
            return fileService.resolvePathToFile(path);
        }
    }

    private void ensureSrcResourceExists(String from) throws ResourceNotFoundException {
        findResourceInfo(from);
    }

    private void ensureTargetResourceNotExists(String to) throws ResourceAlreadyExistsException {
        try {
            findResourceInfo(to);
            throw new ResourceAlreadyExistsException("Resource along the path already exists");
        } catch (ResourceNotFoundException ignored) {}
    }

    private void createNecessaryFolders(String unnecessaryPart, String path) {
        String[] names = path.substring(unnecessaryPart.length()).split("/");
        StringBuilder folder = new StringBuilder(unnecessaryPart);
        for (int i = 0; i < names.length - 1; i++) {
            folder.append(names[i]).append("/");
            folderService.createFolder(folder.toString());
        }
    }

    public byte[] getResourceBinaryContent(Long userId, String path) throws ResourceNotFoundException, IOException {
        StatObjectResponse resourceInfo = findResourceInfo(redirectToUserRootFolder(userId, path));
        if (getResourceType(resourceInfo.object()) == ResourceType.FILE) {
            return fileService.getFileBinaryContent(resourceInfo.object());
        }
        else {
            return folderService.getFolderBinaryContentZipped(resourceInfo.object());
        }
    }

    public String resolveDownloadedResourceName(String path) {
        Path abstractPath = Paths.get(path);
        if (getResourceType(path) == ResourceType.FILE) {
            return abstractPath.getFileName().toString();
        }
        return abstractPath.getFileName().toString() + ".zip";
    }

    public ResourceDto getResourceInfo(Long userId, String path) throws ResourceNotFoundException {
        StatObjectResponse resourceInfo = findResourceInfo(redirectToUserRootFolder(userId, path));
        if (getResourceType(resourceInfo.object()) == ResourceType.FILE) {
            return fileService.mapFileToDto(resourceInfo.object(), resourceInfo.size());
        }
        else {
            return folderService.mapFolderToDto(resourceInfo.object());
        }
    }

    public void removeResource(Long userId, String path) throws ResourceNotFoundException {
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

    private String redirectToUserRootFolder(Long userId, String path) {
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
