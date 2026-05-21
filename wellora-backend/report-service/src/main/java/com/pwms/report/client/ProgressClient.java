package com.pwms.report.client;

import com.pwms.report.dto.ProgressSummaryDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name     = "progress-service",
        path     = "/api/progress",
        fallback = ProgressClientFallback.class
)
public interface ProgressClient {

    @GetMapping("/summary/{patientId}/plan/{planId}")
    ProgressSummaryDTO getDailySummary(
            @PathVariable int patientId,
            @PathVariable int planId);
}