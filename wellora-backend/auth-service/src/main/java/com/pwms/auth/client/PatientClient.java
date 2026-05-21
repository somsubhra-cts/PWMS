package com.pwms.auth.client;

import lombok.Data;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Feign client that calls patient-service directly (via Eureka, NOT gateway)
 * to verify a patient's identity during self-registration.
 */
@FeignClient(name = "patient-service", path = "/api/patients")
public interface PatientClient {

    @GetMapping("/{id}")
    PatientVerifyDTO getPatientById(@PathVariable("id") int id);

    /** Minimal projection — only fields needed for registration verification. */
    @Data
    class PatientVerifyDTO {
        private int    patientId;
        private String patientName;
        private String email;
    }
}
