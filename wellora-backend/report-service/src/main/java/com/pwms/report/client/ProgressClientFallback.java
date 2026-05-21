package com.pwms.report.client;

import com.pwms.report.dto.ProgressSummaryDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@Slf4j
public class ProgressClientFallback implements ProgressClient {

    @Override
    public ProgressSummaryDTO getDailySummary(int patientId, int planId) {
        log.warn("Circuit OPEN — progress-service unavailable. " +
                "Returning fallback summary for patientId: {}", patientId);
        return ProgressSummaryDTO.builder()
                .patientId(patientId)
                .planId(planId)
                .date(LocalDate.now())
                .totalActivities(0)
                .completedActivities(0)
                .completionPercentage(0.0)
                .build();
    }
}