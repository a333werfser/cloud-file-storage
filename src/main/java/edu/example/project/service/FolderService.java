package edu.example.project.service;

import edu.example.project.dto.ResourceDto;
import edu.example.project.exception.BadResourceTypeException;
import edu.example.project.exception.ResourceNotFoundException;
import io.minio.Result;
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
import java.util.Iterator;
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
        try {
            if (pathHasObjectsInside(from)) {
                for (Result<Item> result : minioService.listObjects(from, true)) {
                    Item item = result.get();
                    String destPath = resolveDestinationPath(item.objectName(), from, to);
                    minioService.copyObject(item.objectName(), destPath);
                    minioService.statObject(destPath);
                    minioService.removeObject(item.objectName());
                }
            } else {
                minioService.copyObject(from, to);
                minioService.statObject(to);
                minioService.removeObject(from);
            }
        } catch (ResourceNotFoundException exception) {
            throw new RuntimeException("Copy failed", exception);
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    protected byte[] getFolderBinaryContentZipped(String path) throws IOException {
        ensureFolderPath(path);
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        try (BufferedOutputStream bufferedOut = new BufferedOutputStream(byteOut);
             ZipOutputStream zipOut = new ZipOutputStream(bufferedOut)
        ) {
            if (pathHasObjectsInside(path)) {
                for (Result<Item> result : minioService.listObjects(path, true)) {
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
                }
            } else {
                zipOut.putNextEntry(new ZipEntry(resolveFolderName(path)));
                zipOut.write(new byte[0]);
                zipOut.closeEntry();
            }
        }
        return byteOut.toByteArray();
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

    /**
     * return false, if path has only one empty folder object
     * @param path
     * @return
     */
    protected boolean pathHasObjectsInside(String path) {
        ensureFolderPath(path);
        try {
            for (Result<Item> result : minioService.listObjects(path, true)) {
                Item item = result.get();
                if (!item.objectName().equals(path)) {
                    return true;
                }
            }
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
        return false;
    }

    private String resolveDestinationPath(String objectKey, String from, String to) {
        String relativePath = objectKey.substring(from.length() - 1);
        return to + relativePath;
    }

    private String resolveZipEntryName(String pathToFolder, String fullPath) {
        return fullPath.substring(pathToFolder.length());
    }

    private int countFoldersNumber(String path) {
        return path.length() - path.replace("/", "").length();
    }

    protected String resolvePathToFolder(String path) {
        ensureFolderPath(path);
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

    protected String resolveRootElement(String path) {
        if (!path.contains("/")) {
            return path;
        }
        else {
            return path.substring(0, path.indexOf("/") + 1);
        }
    }

    protected void ensureFolderPath(String... paths) {
        for (String path : paths) {
            if (!path.endsWith("/")) {
                if (!path.isEmpty()) {
                    throw new BadResourceTypeException("Path to folder expected");
                }
            }
        }
    }

}
