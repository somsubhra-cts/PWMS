package com.pwms.patient.integration;

import com.pwms.patient.model.Patient;
import com.pwms.patient.repository.PatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
// Replace real DB with H2 in-memory
@AutoConfigureTestDatabase(replace = Replace.ANY)
// Disable Eureka and cloud configs that break test context
@TestPropertySource(properties = {
        "eureka.client.enabled=false",
        "spring.cloud.discovery.enabled=false",
        "spring.cloud.openfeign.circuitbreaker.enabled=false",
        "management.health.circuitbreakers.enabled=false"
})
class PatientRepositoryIntegrationTest {

    @Autowired
    private PatientRepository patientRepo;

    private Patient patient;

    @BeforeEach
    void setUp() {
        patientRepo.deleteAll();

        patient = new Patient();
        patient.setPatientName("Ashok");
        patient.setAge(21);
        patient.setEmail("ashok@gmail.com");
        patient.setMedicalHistory("None");
    }

    @Test
    void save_andFindById_success() {
        Patient saved = patientRepo.save(patient);

        Optional<Patient> found = patientRepo.findById(saved.getPatientId());

        assertTrue(found.isPresent());
        assertEquals("Ashok", found.get().getPatientName());
    }

    @Test
    void existsByEmail_returnsTrue() {
        patientRepo.save(patient);

        assertTrue(patientRepo.existsByEmail("ashok@gmail.com"));
    }

    @Test
    void existsByEmail_returnsFalse_whenNotExists() {
        assertFalse(patientRepo.existsByEmail("unknown@gmail.com"));
    }

    @Test
    void findByPatientName_success() {
        patientRepo.save(patient);

        Patient found = patientRepo.findByPatientName("Ashok");

        assertNotNull(found);
        assertEquals("ashok@gmail.com", found.getEmail());
    }

    @Test
    void findByPatientName_returnsNull_whenNotExists() {
        Patient found = patientRepo.findByPatientName("Unknown");
        assertNull(found);
    }

    @Test
    void findAll_returnsAllPatients() {
        Patient p2 = new Patient();
        p2.setPatientName("Priya");
        p2.setAge(25);
        p2.setEmail("priya@gmail.com");

        patientRepo.save(patient);
        patientRepo.save(p2);

        List<Patient> all = patientRepo.findAll();
        assertEquals(2, all.size());
    }

    @Test
    void deleteById_removesPatient() {
        Patient saved = patientRepo.save(patient);
        patientRepo.deleteById(saved.getPatientId());

        assertFalse(patientRepo.existsById(saved.getPatientId()));
    }
}