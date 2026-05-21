package com.pwms.patient.service;

import com.pwms.patient.client.NotificationClient;
import com.pwms.patient.exception.PatientAlreadyExistsException;
import com.pwms.patient.exception.PatientNotFoundException;
import com.pwms.patient.interfaces.PatientIntf;
import com.pwms.patient.model.Patient;
import com.pwms.patient.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PatientService implements PatientIntf {

    private final PatientRepository  patientRepo;
    private final NotificationClient notificationClient;

    private static final int DEFAULT_ADMIN_ID = 1;

    @Override
    public Patient addPatient(Patient patient)
            throws PatientAlreadyExistsException {
        log.debug("Adding patient with email: {}", patient.getEmail());

        if (patientRepo.existsByEmail(patient.getEmail())) {
            log.warn("Patient already exists with email: {}",
                    patient.getEmail());
            throw new PatientAlreadyExistsException(
                    "Patient already exists with email: " + patient.getEmail());
        }

        Patient saved = patientRepo.save(patient);
        log.info("Patient created — id: {} name: {}",
                saved.getPatientId(), saved.getPatientName());

        try {
            notificationClient.notifyNewPatientRegistered(
                    saved.getPatientId(), DEFAULT_ADMIN_ID);
            log.debug("Admin notified of new patient id: {}",
                    saved.getPatientId());
        } catch (Exception e) {
            log.error("Failed to notify admin of new patient id: {} — {}",
                    saved.getPatientId(), e.getMessage());
        }

        return saved;
    }

    @Override
    public Patient findPatientById(int patientId)
            throws PatientNotFoundException {
        log.debug("Finding patient by id: {}", patientId);
        return patientRepo.findById(patientId)
                .orElseThrow(() -> {
                    log.warn("Patient not found with id: {}", patientId);
                    return new PatientNotFoundException(
                            "Patient not found with id: " + patientId);
                });
    }

    @Override
    public Patient findPatientByName(String patientName)
            throws PatientNotFoundException {
        log.debug("Finding patient by name: {}", patientName);
        Patient patient = patientRepo.findByPatientName(patientName);
        if (patient == null) {
            log.warn("Patient not found with name: {}", patientName);
            throw new PatientNotFoundException(
                    "Patient not found with name: " + patientName);
        }
        return patient;
    }

    @Override
    public Patient updatePatientById(int patientId, Patient updatedPatient)
            throws PatientNotFoundException {
        log.debug("Updating patient id: {}", patientId);
        findPatientById(patientId);
        updatedPatient.setPatientId(patientId);
        Patient saved = patientRepo.save(updatedPatient);
        log.info("Patient updated — id: {}", patientId);
        return saved;
    }

    @Override
    public void deletePatientById(int patientId)
            throws PatientNotFoundException {
        log.debug("Deleting patient id: {}", patientId);
        if (!patientRepo.existsById(patientId)) {
            log.warn("Cannot delete — patient not found with id: {}",
                    patientId);
            throw new PatientNotFoundException(
                    "Patient not found with id: " + patientId);
        }
        patientRepo.deleteById(patientId);
        log.info("Patient deleted — id: {}", patientId);
    }

    @Override
    public List<Patient> findAllPatients() {
        log.debug("Fetching all patients");
        List<Patient> patients = patientRepo.findAll();
        log.debug("Found {} patients", patients.size());
        return patients;
    }
}