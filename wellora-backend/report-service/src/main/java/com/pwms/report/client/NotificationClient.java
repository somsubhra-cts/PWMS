package com.pwms.report.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
        name     = "notification-service",
        path     = "/api/notifications/internal",
        fallback = NotificationClientFallback.class
)
public interface NotificationClient {

    @PostMapping("/report-shared")
    void notifyReportShared(
            @RequestParam int patientId,
            @RequestParam int planId);

    @PostMapping("/appointment-reminder")
    void notifyAppointmentReminder(
            @RequestParam int patientId,
            @RequestParam int planId);
}