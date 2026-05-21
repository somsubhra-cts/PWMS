package com.cts.WellnessPlanManagementModule.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class NotificationClientFallback implements NotificationClient {

    @Override
    public void notifyPlanAssigned(int patientId, int planId,
                                   String planName) {
        log.warn("Circuit OPEN — notification-service unavailable. " +
                        "Skipping PLAN_ASSIGNED notification — " +
                        "patientId: {} planId: {} plan: {}",
                patientId, planId, planName);
    }
}