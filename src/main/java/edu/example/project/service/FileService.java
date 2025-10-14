package edu.example.project.service;

import edu.example.project.dto.ResourceDto;
import edu.example.project.exception.BadResourceTypeException;
import io.minio.StatObjectResponse;
import org.springframework.stereotype.Service;

@Service
public class FileService implements PathResolverService {

    protected ResourceDto mapFileToDto(String path, Long size) {
        ResourceDto resourceDto = new ResourceDto();
        path = eraseUserRootFolder(path);
        resourceDto.setPath(resolvePathToFile(path));
        resourceDto.setName(resolveFileName(path));
        resourceDto.setSize(size);
        resourceDto.setType("FILE");
        return resourceDto;
    }

    private String resolvePathToFile(String path) {
        if (path.endsWith("/")) {
            throw new BadResourceTypeException("Path to file expected");
        }
        if (path.contains("/")) {
            return path.substring(0, path.lastIndexOf("/") + 1);
        }
        else {
            return "";
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

}
