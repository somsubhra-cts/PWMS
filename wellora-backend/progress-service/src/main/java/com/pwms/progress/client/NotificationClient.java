package com.pwms.progress.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
        name     = "notification-service",
        path     = "/api/notifications/internal",
        fallback = NotificationClientFallback.class
)
public interface NotificationClient {

    @PostMapping("/activity-appreciation")
    void notifyActivityAppreciation(
            @RequestParam int patientId,
            @RequestParam int planId,
            @RequestParam String activityName);

    @PostMapping("/activity-reminder")
    void notifyActivityReminder(
            @RequestParam int patientId,
            @RequestParam int planId,
            @RequestParam String activityName);

    @PostMapping("/plan-completed")
    void notifyPlanCompleted(
            @RequestParam int patientId,
            @RequestParam int planId,
            @RequestParam int adminId);

    @PostMapping("/weekly-summary")
    void notifyWeeklySummary(
            @RequestParam int patientId,
            @RequestParam int planId,
            @RequestParam double completionPct);

    @PostMapping("/appointment-reminder")
    void notifyAppointmentReminder(
            @RequestParam int patientId,
            @RequestParam int planId);
}

