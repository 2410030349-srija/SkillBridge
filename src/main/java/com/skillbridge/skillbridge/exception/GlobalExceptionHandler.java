package com.skillbridge.skillbridge.exception; // NOSONAR - false positive: package is named

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.context.MessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException ex) {
        Map<String, String> body = new LinkedHashMap<>();
        body.put("error", ex.getMessage());
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> body = new LinkedHashMap<>();
        body.put("error", ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
            .map(MessageSourceResolvable::getDefaultMessage)
                .orElse("Validation failed"));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Map<String, String>> handleAuthentication(AuthenticationException ex) {
        Map<String, String> body = new LinkedHashMap<>();
        body.put("error", "Invalid email or password");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
    }
}