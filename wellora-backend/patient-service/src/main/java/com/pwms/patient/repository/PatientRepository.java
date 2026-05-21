package com.pwms.patient.repository;

import com.pwms.patient.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Integer> {

    Patient findByPatientName(String patientName);
    boolean existsByEmail(String email);
}