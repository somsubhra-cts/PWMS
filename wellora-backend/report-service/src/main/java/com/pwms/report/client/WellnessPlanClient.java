package com.pwms.report.client;

import com.pwms.report.dto.PlanAssignmentDTO;
import com.pwms.report.dto.PlanDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(
        name     = "wellnessplan-service",
        path     = "/api/plans",
        fallback = WellnessPlanClientFallback.class
)
public interface WellnessPlanClient {

    @GetMapping("/{planId}")
    PlanDTO getPlanById(@PathVariable int planId);

    @GetMapping("/assignments/patient/{patientId}")
    List<PlanAssignmentDTO> getActiveAssignmentsByPatient(
            @PathVariable int patientId);
}
