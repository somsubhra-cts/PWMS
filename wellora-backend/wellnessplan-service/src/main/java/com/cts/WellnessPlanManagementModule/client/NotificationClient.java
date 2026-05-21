package com.cts.WellnessPlanManagementModule.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
        name     = "notification-service",
        path     = "/api/notifications/internal",
        fallback = NotificationClientFallback.class
)
public interface NotificationClient {

    // POST /api/notifications/internal/plan-assigned
    // Called when admin assigns a plan to a patient
    @PostMapping("/plan-assigned")
    void notifyPlanAssigned(
            @RequestParam int patientId,
            @RequestParam int planId,
            @RequestParam String planName);
}
