package edu.example.project.exception;

public class ResourceNotFoundException extends Exception {

    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
