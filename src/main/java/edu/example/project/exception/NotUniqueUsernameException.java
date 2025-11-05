package edu.example.project.exception;

public class NotUniqueUsernameException extends RuntimeException {

    public NotUniqueUsernameException(String message, Throwable cause) {
        super(message, cause);
    }
}
