package com.cts.WellnessPlanManagementModule.Exception;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.IOException;
import java.util.Date;

@RestControllerAdvice
public class GlobalException
{
    @ExceptionHandler(WellNessPlanAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> wellnessPlanAlreadyExists(HttpServletResponse response, HttpServletRequest req, WellNessPlanAlreadyExistsException ex) throws IOException {

        ErrorResponse errorResponse=new ErrorResponse(new Date(),HttpStatus.CONFLICT.value(),"Plan Already Exists",ex.getMessage());
        return new ResponseEntity(errorResponse,HttpStatus.CONFLICT);

    }

    @ExceptionHandler(PatientPlanNotExistsException.class)

    public ResponseEntity<ErrorResponse> wellnessPlanNotPresent(HttpServletResponse response,HttpServletRequest request,PatientPlanNotExistsException ex)

    {
        ErrorResponse errorResponse=new ErrorResponse(new Date(),HttpStatus.BAD_REQUEST.value(),"Plan Not Found",ex.getMessage());
        return new ResponseEntity(errorResponse,HttpStatus.BAD_REQUEST);
    }



    @ExceptionHandler(PatientNotFoundException.class)

    public ResponseEntity<ErrorResponse> PatientNotFound(HttpServletResponse response,HttpServletRequest request,PatientNotFoundException ex)

    {
        ErrorResponse errorResponse=new ErrorResponse(new Date(),HttpStatus.BAD_REQUEST.value(),"Not Found",ex.getMessage());
        return new ResponseEntity(errorResponse,HttpStatus.BAD_REQUEST);
    }


}
