package com.pwms.report.client;

import com.pwms.report.dto.PatientDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name     = "patient-service",
        path     = "/api/patients",
        fallback = PatientClientFallback.class
)
public interface PatientClient {

    @GetMapping("/{patientId}")
    PatientDTO getPatientById(@PathVariable int patientId);
}