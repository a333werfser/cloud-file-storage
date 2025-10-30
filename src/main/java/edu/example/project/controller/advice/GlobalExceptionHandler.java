package edu.example.project.controller.advice;

import edu.example.project.dto.ResponseMessage;
import edu.example.project.exception.BadResourceTypeException;
import edu.example.project.exception.ResourceAlreadyExistsException;
import edu.example.project.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.Objects;

@ControllerAdvice
public class GlobalExceptionHandler {

    @Value("${spring.servlet.multipart.max-file-size}")
    private String maxSize;

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseMessage> handle(MethodArgumentNotValidException ex) {
        ResponseMessage message = new ResponseMessage();
        message.setMessage(Objects.requireNonNull(ex.getFieldError()).getDefaultMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(message);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ResponseMessage> handle(MaxUploadSizeExceededException ex) {
        ResponseMessage message = new ResponseMessage();
        message.setMessage("Max file size: " + maxSize);
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(message);
    }

    private ResponseEntity<ResponseMessage> buildErrorResponseMessage(Throwable ex, HttpStatus status) {
        ResponseMessage message = new ResponseMessage();
        message.setMessage(ex.getMessage());
        return ResponseEntity.status(status).body(message);
    }

    @ExceptionHandler(BadResourceTypeException.class)
    public ResponseEntity<ResponseMessage> handle(BadResourceTypeException ex) {
        return buildErrorResponseMessage(ex, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ResourceAlreadyExistsException.class)
    public ResponseEntity<ResponseMessage> handle(ResourceAlreadyExistsException ex) {
        return buildErrorResponseMessage(ex, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ResponseMessage> handle(ResourceNotFoundException ex) {
        return buildErrorResponseMessage(ex, HttpStatus.NOT_FOUND);
    }

//    @ExceptionHandler(Exception.class)
//    public ResponseEntity<ResponseMessage> handleAll(Exception ex) {
//        if (ex instanceof AuthenticationException) {
//            throw (AuthenticationException) ex; // return AuthenticationException to ExceptionTranslationFilter
//        }
//        ResponseMessage message = new ResponseMessage();
//        message.setMessage("Internal Server Error");
//        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(message);
//    }
}
