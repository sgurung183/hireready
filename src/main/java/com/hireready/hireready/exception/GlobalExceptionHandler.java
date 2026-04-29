package com.hireready.hireready.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

// @RestControllerAdvice watches every controller in the app.
// whenever an exception is thrown anywhere, Spring checks this class first
// and runs the matching @ExceptionHandler method instead of returning a 500.
@RestControllerAdvice
public class GlobalExceptionHandler {

    // thrown when a resume or user is not found in the DB
    // returns 404 Not Found so the frontend knows the resource doesn't exist
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<String> handleNotFound(ResourceNotFoundException ex){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    // thrown when login fails — password doesn't match
    // returns 401 Unauthorized so the frontend knows to show an invalid credentials message
    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<String> handleInvalidCredentials(InvalidCredentialsException ex){
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ex.getMessage());
    }

    // Spring throws this automatically when @Valid fails on a request DTO — we never throw it ourselves
    // a single request can fail multiple validations at once (e.g. empty email AND empty password)
    // so we loop through all field errors, format each as "fieldName: error message",
    // join them into one string, and return 400 Bad Request so the frontend knows which fields failed
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleValidation(MethodArgumentNotValidException ex){
        String message = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(message);
    }
}
