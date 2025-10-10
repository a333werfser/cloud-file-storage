package edu.example.project.exception;

public class BadResourceTypeException extends RuntimeException {

    public BadResourceTypeException(String message, Throwable cause) {
        super(message, cause);
    }

    public BadResourceTypeException(String message) {
        super(message);
    }
}
