package edu.example.project.service;

public interface PathResolver {

    default String eraseUserRootFolder(String path) {
        return path.substring(path.indexOf("/") + 1);
    }

}
