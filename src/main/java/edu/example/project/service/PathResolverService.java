package edu.example.project.service;

public interface PathResolverService {

    default String eraseUserRootFolder(String path) {
        return path.substring(path.indexOf("/") + 1);
    }

}
