package com.pwms.patient.exception;

public class PatientAlreadyExistsException extends Exception {
    public PatientAlreadyExistsException(String message) {
        super(message);
    }
}
