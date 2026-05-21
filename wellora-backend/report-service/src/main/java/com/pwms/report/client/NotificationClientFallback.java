package com.pwms.report.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class NotificationClientFallback implements NotificationClient {

    @Override
    public void notifyReportShared(int patientId, int planId) {
        log.warn("Circuit OPEN — notification-service unavailable. " +
                        "Skipping report shared notification for patientId: {}",
                patientId);
    }

    @Override
    public void notifyAppointmentReminder(int patientId, int planId) {
        log.warn("Circuit OPEN — notification-service unavailable. " +
                        "Skipping appointment reminder for patientId: {}",
                patientId);
    }
}