package edu.example.project.service;

import edu.example.project.config.BucketProperties;
import edu.example.project.dto.ResourceDto;
import edu.example.project.exception.BadResourceTypeException;
import io.minio.ListObjectsArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.Result;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;

@Service
@RequiredArgsConstructor
public class FolderService implements PathResolverService {

    private final MinioClient minioClient;

    private final BucketProperties bucketProperties;

    protected void createFolder(String path) {
        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketProperties.getDefaultName())
                            .object(path)
                            .stream(new ByteArrayInputStream(new byte[]{}), 0, -1)
                            .build()
            );
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    protected ResourceDto mapFolderToDto(String path) {
        ResourceDto resourceDto = new ResourceDto();
        path = eraseUserRootFolder(path);
        resourceDto.setPath(resolvePathToFolder(path));
        resourceDto.setName(resolveFolderName(path));
        resourceDto.setType("DIRECTORY");
        return resourceDto;
    }

    public boolean virtualFolderExists(String path) {
        Iterable<Result<Item>> objects = minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(bucketProperties.getDefaultName())
                        .prefix(path)
                        .maxKeys(1)
                        .build()
        );
        return objects.iterator().hasNext();
    }

    private String resolvePathToFolder(String path) {
        if (!path.endsWith("/")) {
            throw new BadResourceTypeException("Path to folder expected");
        }
        if (countFoldersNumber(path) == 1) {
            return "";
        }
        else {
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
        }
        else {
            String pathWithoutLastSlash = path.substring(0, path.lastIndexOf("/"));
            return pathWithoutLastSlash.substring(pathWithoutLastSlash.lastIndexOf("/") + 1) + "/";
        }
    }

    private int countFoldersNumber(String path) {
        return path.length() - path.replace("/", "").length();
    }

}
