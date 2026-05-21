package com.pwms.patient.exception;

import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice(basePackages = "com.pwms.patient")
public class PatientExceptionHandler {

    // ── Validation errors — 400 ───────────────────────────────
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleValidationErrors(
            MethodArgumentNotValidException ex) {

        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String field   = ((FieldError) error).getField();
            String message = error.getDefaultMessage();
            fieldErrors.put(field, message);
        });

        return Map.of(
                "timestamp", LocalDateTime.now().toString(),
                "status",    400,
                "error",     "Validation Failed",
                "fields",    fieldErrors
        );
    }

    // ── Patient not found — 404 ───────────────────────────────
    @ExceptionHandler(PatientNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, Object> handleNotFound(PatientNotFoundException ex) {
        return Map.of(
                "timestamp", LocalDateTime.now().toString(),
                "status",    404,
                "error",     "Not Found",
                "message",   ex.getMessage()
        );
    }

    // ── Patient already exists — 409 ─────────────────────────
    @ExceptionHandler(PatientAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, Object> handleAlreadyExists(
            PatientAlreadyExistsException ex) {
        return Map.of(
                "timestamp", LocalDateTime.now().toString(),
                "status",    409,
                "error",     "Conflict",
                "message",   ex.getMessage()
        );
    }
}