package com.pwms.patient.unit;

import com.pwms.patient.client.NotificationClient;
import com.pwms.patient.exception.PatientAlreadyExistsException;
import com.pwms.patient.exception.PatientNotFoundException;
import com.pwms.patient.model.Patient;
import com.pwms.patient.repository.PatientRepository;
import com.pwms.patient.service.PatientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PatientServiceTest {

    @Mock
    private PatientRepository patientRepo;

    @Mock
    private NotificationClient notificationClient;

    @InjectMocks
    private PatientService patientService;

    private Patient patient;

    @BeforeEach
    void setUp() {
        patient = new Patient();
        patient.setPatientId(1);
        patient.setPatientName("Ashok");
        patient.setAge(21);
        patient.setEmail("ashok@gmail.com");
        patient.setMedicalHistory("None");
    }

    // ── addPatient ────────────────────────────────────────────

    @Test
    void addPatient_success() throws PatientAlreadyExistsException {
        when(patientRepo.existsByEmail("ashok@gmail.com")).thenReturn(false);
        when(patientRepo.save(patient)).thenReturn(patient);
        doNothing().when(notificationClient)
                .notifyNewPatientRegistered(anyInt(), anyInt());

        Patient result = patientService.addPatient(patient);

        assertNotNull(result);
        assertEquals("Ashok", result.getPatientName());
        verify(patientRepo, times(1)).save(patient);
        verify(notificationClient, times(1))
                .notifyNewPatientRegistered(anyInt(), anyInt());
    }

    @Test
    void addPatient_alreadyExists_throwsException() {
        when(patientRepo.existsByEmail("ashok@gmail.com")).thenReturn(true);

        assertThrows(PatientAlreadyExistsException.class,
                () -> patientService.addPatient(patient));

        verify(patientRepo, never()).save(any());
    }

    // ── findPatientById ───────────────────────────────────────

    @Test
    void findPatientById_success() throws PatientNotFoundException {
        when(patientRepo.findById(1)).thenReturn(Optional.of(patient));

        Patient result = patientService.findPatientById(1);

        assertEquals(1, result.getPatientId());
        assertEquals("Ashok", result.getPatientName());
    }

    @Test
    void findPatientById_notFound_throwsException() {
        when(patientRepo.findById(99)).thenReturn(Optional.empty());

        assertThrows(PatientNotFoundException.class,
                () -> patientService.findPatientById(99));
    }

    // ── findPatientByName ─────────────────────────────────────

    @Test
    void findPatientByName_success() throws PatientNotFoundException {
        when(patientRepo.findByPatientName("Ashok")).thenReturn(patient);

        Patient result = patientService.findPatientByName("Ashok");

        assertEquals("Ashok", result.getPatientName());
    }

    @Test
    void findPatientByName_notFound_throwsException() {
        when(patientRepo.findByPatientName("Unknown")).thenReturn(null);

        assertThrows(PatientNotFoundException.class,
                () -> patientService.findPatientByName("Unknown"));
    }

    // ── findAllPatients ───────────────────────────────────────

    @Test
    void findAllPatients_success() {
        when(patientRepo.findAll()).thenReturn(List.of(patient));

        List<Patient> result = patientService.findAllPatients();

        assertEquals(1, result.size());
        assertEquals("Ashok", result.get(0).getPatientName());
    }

    // ── updatePatient ─────────────────────────────────────────

    @Test
    void updatePatient_success() throws PatientNotFoundException {
        Patient updated = new Patient();
        updated.setPatientName("Ashok Kumar");
        updated.setAge(22);
        updated.setEmail("ashok@gmail.com");

        when(patientRepo.findById(1)).thenReturn(Optional.of(patient));
        when(patientRepo.save(any(Patient.class))).thenReturn(updated);

        Patient result = patientService.updatePatientById(1, updated);

        assertEquals("Ashok Kumar", result.getPatientName());
        verify(patientRepo, times(1)).save(any(Patient.class));
    }

    // ── deletePatient ─────────────────────────────────────────

    @Test
    void deletePatient_success() throws PatientNotFoundException {
        when(patientRepo.existsById(1)).thenReturn(true);
        doNothing().when(patientRepo).deleteById(1);

        assertDoesNotThrow(() -> patientService.deletePatientById(1));
        verify(patientRepo, times(1)).deleteById(1);
    }

    @Test
    void deletePatient_notFound_throwsException() {
        when(patientRepo.existsById(99)).thenReturn(false);

        assertThrows(PatientNotFoundException.class,
                () -> patientService.deletePatientById(99));

        verify(patientRepo, never()).deleteById(any());
    }
}