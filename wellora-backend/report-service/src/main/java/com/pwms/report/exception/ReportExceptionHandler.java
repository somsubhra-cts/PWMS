package com.pwms.report.exception;

import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice(basePackages = "com.pwms.report")
public class ReportExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleValidation(
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




    @ExceptionHandler(ReportNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, Object> handleNotFound(ReportNotFoundException ex) {
        return Map.of(
                "timestamp", LocalDateTime.now().toString(),
                "status",    404,
                "error",     "Not Found",
                "message",   ex.getMessage()
        );
    }
//    @ExceptionHandler(ReportNotFoundException.class)
//    @ResponseStatus(HttpStatus.OK)
//    public Map<String, Object> handleNotFound(ReportNotFoundException ex) {
//        return Map.of(
//                "timestamp", LocalDateTime.now().toString(),
//                "status",    200,
//                "message",   "SUCCESS"
//        );
//    }
}