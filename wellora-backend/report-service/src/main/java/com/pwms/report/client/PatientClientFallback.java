package com.pwms.report.client;

import com.pwms.report.dto.PatientDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class PatientClientFallback implements PatientClient {

    @Override
    public PatientDTO getPatientById(int patientId) {
        log.warn("Circuit OPEN — patient-service unavailable. " +
                "Returning fallback for patientId: {}", patientId);
        return PatientDTO.builder()
                .patientId(patientId)
                .patientName("Unavailable")
                .build();
    }
}
