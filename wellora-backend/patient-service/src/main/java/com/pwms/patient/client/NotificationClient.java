package com.pwms.patient.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
        name     = "notification-service",
        path     = "/api/notifications/internal",
        fallback = NotificationClientFallback.class   // ← fallback when circuit opens
)
public interface NotificationClient {

    @PostMapping("/new-patient")
    void notifyNewPatientRegistered(
            @RequestParam int patientId,
            @RequestParam int adminId);
}