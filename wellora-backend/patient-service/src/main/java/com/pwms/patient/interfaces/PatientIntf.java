package com.pwms.patient.interfaces;

import com.pwms.patient.model.Patient;
import com.pwms.patient.exception.PatientAlreadyExistsException;
import com.pwms.patient.exception.PatientNotFoundException;

import java.util.List;

public interface PatientIntf {

    Patient addPatient(Patient patient) throws PatientAlreadyExistsException;

    Patient findPatientById(int patientId) throws PatientNotFoundException;

    Patient findPatientByName(String patientName) throws PatientNotFoundException;

    Patient updatePatientById(int patientId, Patient updatedPatient) throws PatientNotFoundException;

    void deletePatientById(int patientId) throws PatientNotFoundException;

    List<Patient> findAllPatients();
}