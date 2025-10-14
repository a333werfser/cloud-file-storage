package edu.example.project.service;

import edu.example.project.dto.ResourceDto;
import edu.example.project.exception.BadResourceTypeException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FolderService implements PathResolverService {

    private final MinioService minioService;

    protected void createFolder(String path) {
        minioService.putEmptyObject(path);
    }

    protected ResourceDto mapFolderToDto(String path) {
        ResourceDto resourceDto = new ResourceDto();
        path = eraseUserRootFolder(path);
        resourceDto.setPath(resolvePathToFolder(path));
        resourceDto.setName(resolveFolderName(path));
        resourceDto.setType("DIRECTORY");
        return resourceDto;
    }

    protected boolean pathHasObjectsInside(String path) {
        return minioService.listObjects(path, 1).iterator().hasNext();
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
