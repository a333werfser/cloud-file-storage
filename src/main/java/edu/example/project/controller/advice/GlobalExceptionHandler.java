package edu.example.project.controller.advice;

import edu.example.project.dto.ResponseMessageDto;
import edu.example.project.exception.BadResourceTypeException;
import edu.example.project.exception.ResourceAlreadyExistsException;
import edu.example.project.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@ControllerAdvice
public class GlobalExceptionHandler {

    @Value("${spring.servlet.multipart.max-file-size}")
    private String maxSize;

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ResponseMessageDto> handleMaxSizeException(MaxUploadSizeExceededException ex) {
        ResponseMessageDto message = new ResponseMessageDto();
        message.setMessage("Max file size: " + maxSize);
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(message);
    }

    @ExceptionHandler(BadResourceTypeException.class)
    public ResponseEntity<ResponseMessageDto> handleBadResourceTypeException(BadResourceTypeException ex) {
        return buildErrorResponseMessage(ex, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ResourceAlreadyExistsException.class)
    public ResponseEntity<ResponseMessageDto> handleResourceAlreadyExistsException(ResourceAlreadyExistsException ex) {
        return buildErrorResponseMessage(ex, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ResponseMessageDto> handleResourceNotFoundExceptions(ResourceNotFoundException ex) {
        return buildErrorResponseMessage(ex, HttpStatus.NOT_FOUND);
    }

    private ResponseEntity<ResponseMessageDto> buildErrorResponseMessage(Throwable ex, HttpStatus status) {
        ResponseMessageDto message = new ResponseMessageDto();
        message.setMessage(ex.getMessage());
        return ResponseEntity.status(status).body(message);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseMessageDto> handleAllExceptions(Exception ex) {
        ResponseMessageDto message = new ResponseMessageDto();
        message.setMessage("Internal Server Error");

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(message);
    }
}
