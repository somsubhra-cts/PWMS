package com.pwms.report.client;

import com.pwms.report.dto.PlanAssignmentDTO;
import com.pwms.report.dto.PlanDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class WellnessPlanClientFallback implements WellnessPlanClient {

    @Override
    public PlanDTO getPlanById(int planId) {
        log.warn("Circuit OPEN — wellnessplan-service unavailable. " +
                "Returning fallback for planId: {}", planId);
        return PlanDTO.builder()
                .planId(planId)
                .planName("Unavailable")
                .build();
    }

    @Override
    public List<PlanAssignmentDTO> getActiveAssignmentsByPatient(
            int patientId) {
        log.warn("Circuit OPEN — wellnessplan-service unavailable. " +
                "Returning empty assignments for patientId: {}", patientId);
        return List.of();
    }
}
