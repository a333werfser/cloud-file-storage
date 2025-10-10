package edu.example.project.service;

import edu.example.project.dto.ResourceDto;
import edu.example.project.exception.BadResourceTypeException;
import edu.example.project.exception.ResourceNotFoundException;
import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.Item;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Service
@RequiredArgsConstructor
public class ResourceService {

    private final MinioClient minioClient;

    private static final String DEFAULT_NAME = "user-files";

    @PostConstruct
    private void createRootBucket() throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        String name = DEFAULT_NAME;
        boolean found = bucketExists(name);

        if (!found) {
            createBucket(name);
        }
    }

    private void createBucket(String bucketName) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        minioClient.makeBucket(
                MakeBucketArgs.builder()
                        .bucket(bucketName)
                        .build()
        );
    }

    private boolean bucketExists(String bucketName) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        return minioClient.bucketExists(
                BucketExistsArgs.builder()
                        .bucket(bucketName)
                        .build()
        );
    }

    public ResourceDto getResourceInfo(String path, int userId) throws ResourceNotFoundException {
        String userContextPath = redirectToUserRootFolder(path, userId);
        StatObjectResponse metaobject;
        if (getResourceType(userContextPath) == ResourceType.FILE) {
            try {
                metaobject = getStatObject(userContextPath);
            } catch (ResourceNotFoundException exception) {
                throw new ResourceNotFoundException("File does not exist", exception);
            }
            return mapFileToDto(userContextPath, metaobject);
        }
        else {
            try {
                getStatObject(userContextPath);
            } catch (ResourceNotFoundException exception) {
                if (virtualFolderExists(userContextPath)) {
                    return mapFolderToDto(userContextPath);
                }
                else {
                    throw new ResourceNotFoundException("Directory does not exist", exception);
                }
            }
            return mapFolderToDto(userContextPath);
        }
    }

    private StatObjectResponse getStatObject(String path) throws ResourceNotFoundException {
        try {
            return minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(DEFAULT_NAME)
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
            createFolder(userFolder);
        }
        return userFolder + path;
    }

    private void createFolder(String name) {
        try {
            minioClient.putObject(
                    PutObjectArgs.builder().bucket(DEFAULT_NAME).object(name).stream(
                                    new ByteArrayInputStream(new byte[] {}), 0, -1)
                            .build());
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    private boolean virtualFolderExists(String path) {
        Iterable<Result<Item>> objects = minioClient.listObjects(
                ListObjectsArgs.builder().bucket(DEFAULT_NAME).prefix(path).maxKeys(1).build()
        );
        return objects.iterator().hasNext();
    }

    private ResourceDto mapFolderToDto(String path) {
        ResourceDto resourceDto = new ResourceDto();
        path = eraseUserRootFolder(path);
        resourceDto.setPath(resolvePathToFolder(path));
        resourceDto.setName(resolveFolderName(path));
        resourceDto.setType("DIRECTORY");
        return resourceDto;
    }

    private ResourceDto mapFileToDto(String path, StatObjectResponse object) {
        ResourceDto resourceDto = new ResourceDto();
        path = eraseUserRootFolder(path);
        resourceDto.setPath(resolvePathToFile(path));
        resourceDto.setName(resolveFileName(path));
        resourceDto.setSize(object.size());
        resourceDto.setType("FILE");
        return resourceDto;
    }

    private String resolvePathToFile(String path) {
        if (path.endsWith("/")) {
            throw new BadResourceTypeException("Path to file expected");
        }
        if (path.contains("/")) {
            return path.substring(0, path.lastIndexOf("/") + 1);
        } else {
            return "";
        }
    }

    private String resolvePathToFolder(String path) {
        if (!path.endsWith("/")) {
            throw new BadResourceTypeException("Path to folder expected");
        }
        if (countFoldersNumber(path) == 1) {
            return "";
        } else {
            String pathWithoutLastSlash = path.substring(0, path.lastIndexOf("/"));
            return pathWithoutLastSlash.substring(0, pathWithoutLastSlash.lastIndexOf("/") + 1);
        }
    }

    private String resolveFolderName(String path) {
        if (!path.endsWith("/")) {
            throw new BadResourceTypeException("Path to folder expected");
        }
        if (countFoldersNumber(path) == 1) {
            return path;
        } else {
            String pathWithoutLastSlash = path.substring(0, path.lastIndexOf("/"));
            return pathWithoutLastSlash.substring(pathWithoutLastSlash.lastIndexOf("/") + 1) + "/";
        }
    }

    private String resolveFileName(String path) {
        if (path.endsWith("/")) {
            throw new BadResourceTypeException("Path to file expected");
        }
        if (!path.contains("/")) {
            return path;
        }
        else {
            return path.substring(path.lastIndexOf("/") + 1);
        }
    }

    private String eraseUserRootFolder(String path) {
        return path.substring(path.indexOf("/") + 1);
    }

    private int countFoldersNumber(String path) {
        return path.length() - path.replace("/", "").length();
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
