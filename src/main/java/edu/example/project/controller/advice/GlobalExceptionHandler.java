package edu.example.project.controller.advice;

import edu.example.project.dto.ResponseMessageDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseMessageDto> handleAllExceptions() {
        ResponseMessageDto responseMessageDto = new ResponseMessageDto();
        responseMessageDto.setMessage("Internal Server Error");

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseMessageDto);
    }
}
