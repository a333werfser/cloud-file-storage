package edu.example.project.service;

import edu.example.project.dto.ResourceDto;
import edu.example.project.exception.BadResourceTypeException;
import edu.example.project.exception.ResourceNotFoundException;
import io.minio.errors.*;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@RequiredArgsConstructor
public class FolderService implements PathResolverService {

    private final MinioService minioService;

    protected void moveFolder(String from, String to) {
        ensureFolderPath(from, to);
        if (from.equals(to)) {
            return;
        }
        if (pathHasObjectsInside(from)) {
            minioService.listObjects(from).forEach(result -> {
                try {
                    Item objectInfo = result.get();
                    minioService.copyObject(objectInfo.objectName(), to + objectInfo.objectName());
                    try {
                        minioService.statObject(to);
                    } catch (ResourceNotFoundException exception) {
                        throw new RuntimeException("Copy failed", exception);
                    }
                    minioService.removeObject(objectInfo.objectName());
                } catch (Exception exception) {
                    throw new RuntimeException(exception);
                }
            });
        }
        else {
            minioService.copyObject(from, to);
            minioService.removeObject(from);
        }
    }

    protected byte[] getFolderBinaryContentZipped(String path) throws IOException {
        ensureFolderPath(path);
        try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
             BufferedOutputStream bufferedOut = new BufferedOutputStream(byteOut);
             ZipOutputStream zipOut = new ZipOutputStream(bufferedOut)
        ) {
            if (pathHasObjectsInside(path)) {
                minioService.listObjects(path).forEach((result) -> {
                    try {
                        Item objectInfo = result.get();
                        String key = objectInfo.objectName();
                        try (InputStream objectIn = minioService.getObject(key)) {
                            zipOut.putNextEntry(new ZipEntry(resolveZipEntryName(path, key)));
                            zipOut.write(objectIn.readAllBytes());
                            zipOut.closeEntry();
                        }
                    } catch (Exception exception) {
                        throw new RuntimeException(exception);
                    }
                });
            }
            else {
                zipOut.putNextEntry(new ZipEntry(resolveFolderName(path)));
                zipOut.write(new byte[0]);
                zipOut.closeEntry();
            }
            return byteOut.toByteArray();
        }
    }

    protected ResourceDto mapFolderToDto(String path) {
        ensureFolderPath(path);
        ResourceDto resourceDto = new ResourceDto();
        path = eraseUserRootFolder(path);
        resourceDto.setPath(resolvePathToFolder(path));
        resourceDto.setName(resolveFolderName(path));
        resourceDto.setType("DIRECTORY");
        return resourceDto;
    }

    protected void createFolder(String path) {
        ensureFolderPath(path);
        minioService.putEmptyObject(path);
    }

    protected boolean pathHasObjectsInside(String path) {
        ensureFolderPath(path);
        return minioService.listObjects(path, 1).iterator().hasNext();
    }

    private String resolveZipEntryName(String pathToFolder, String fullPath) {
        return fullPath.substring(pathToFolder.length());
    }

    private int countFoldersNumber(String path) {
        return path.length() - path.replace("/", "").length();
    }

    private String resolvePathToFolder(String path) {
        if (countFoldersNumber(path) == 1) {
            return "";
        }
        else {
            path = path.substring(0, path.lastIndexOf("/"));
            return path.substring(0, path.lastIndexOf("/") + 1);
        }
    }

    private String resolveFolderName(String path) {
        if (countFoldersNumber(path) == 1) {
            return path;
        }
        else {
            path = path.substring(0, path.lastIndexOf("/"));
            return path.substring(path.lastIndexOf("/") + 1) + "/";
        }
    }

    private void ensureFolderPath(String... paths) {
        for (String path : paths) {
            if (!path.endsWith("/")) {
                throw new BadResourceTypeException("Path to folder expected");
            }
        }
    }

}
