package com.pwms.patient.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pwms.patient.exception.PatientAlreadyExistsException;
import com.pwms.patient.exception.PatientNotFoundException;
import com.pwms.patient.interfaces.PatientIntf;
import com.pwms.patient.model.Patient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// @WebMvcTest loads only the web layer — no DB, no service impl
@WebMvcTest(PatientController.class)
@TestPropertySource(properties = {
        "eureka.client.enabled=false",
        "spring.cloud.discovery.enabled=false",
        "spring.cloud.openfeign.circuitbreaker.enabled=false",
        "management.health.circuitbreakers.enabled=false",
        "spring.cloud.config.enabled=false"
})
class PatientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PatientIntf patientService;

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

    // ── POST /api/patients ────────────────────────────────────

    @Test
    void addPatient_returns201() throws Exception {
        when(patientService.addPatient(any(Patient.class)))
                .thenReturn(patient);

        mockMvc.perform(post("/api/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patient)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.patientName").value("Ashok"))
                .andExpect(jsonPath("$.email").value("ashok@gmail.com"));
    }

    @Test
    void addPatient_duplicate_returns409() throws Exception {
        when(patientService.addPatient(any(Patient.class)))
                .thenThrow(new PatientAlreadyExistsException(
                        "Patient already exists with email: ashok@gmail.com"));

        mockMvc.perform(post("/api/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patient)))
                .andExpect(status().isConflict());
    }

    // ── GET /api/patients ─────────────────────────────────────

    @Test
    void getAllPatients_returns200() throws Exception {
        when(patientService.findAllPatients())
                .thenReturn(List.of(patient));

        mockMvc.perform(get("/api/patients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].patientName").value("Ashok"))
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getAllPatients_emptyList_returns200() throws Exception {
        when(patientService.findAllPatients()).thenReturn(List.of());

        mockMvc.perform(get("/api/patients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // ── GET /api/patients/{id} ────────────────────────────────

    @Test
    void getPatientById_returns200() throws Exception {
        when(patientService.findPatientById(1)).thenReturn(patient);

        mockMvc.perform(get("/api/patients/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.patientId").value(1))
                .andExpect(jsonPath("$.patientName").value("Ashok"));
    }

    @Test
    void getPatientById_notFound_returns404() throws Exception {
        when(patientService.findPatientById(99))
                .thenThrow(new PatientNotFoundException(
                        "Patient not found with id: 99"));

        mockMvc.perform(get("/api/patients/99"))
                .andExpect(status().isNotFound());
    }

    // ── GET /api/patients/search?name= ────────────────────────

    @Test
    void getPatientByName_returns200() throws Exception {
        when(patientService.findPatientByName("Ashok"))
                .thenReturn(patient);

        mockMvc.perform(get("/api/patients/search")
                        .param("name", "Ashok"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.patientName").value("Ashok"));
    }

    // ── PUT /api/patients/{id} ────────────────────────────────

    @Test
    void updatePatient_returns200() throws Exception {
        patient.setPatientName("Ashok Kumar");
        when(patientService.updatePatientById(
                eq(1), any(Patient.class))).thenReturn(patient);

        mockMvc.perform(put("/api/patients/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patient)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.patientName").value("Ashok Kumar"));
    }

    // ── DELETE /api/patients/{id} ─────────────────────────────

    @Test
    void deletePatient_returns200() throws Exception {
        doNothing().when(patientService).deletePatientById(1);

        mockMvc.perform(delete("/api/patients/1"))
                .andExpect(status().isOk())
                .andExpect(content().string(
                        "Patient with id 1 deleted successfully."));
    }

    @Test
    void deletePatient_notFound_returns404() throws Exception {
        doThrow(new PatientNotFoundException(
                "Patient not found with id: 99"))
                .when(patientService).deletePatientById(99);

        mockMvc.perform(delete("/api/patients/99"))
                .andExpect(status().isNotFound());
    }
}