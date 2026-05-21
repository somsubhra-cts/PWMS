package com.pwms.patient.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

// Called when notification-service is DOWN or circuit is OPEN
// Patient registration still succeeds — notification is just skipped
@Component
@Slf4j
public class NotificationClientFallback implements NotificationClient {

    @Override
    public void notifyNewPatientRegistered(int patientId, int adminId) {
        log.warn("Circuit OPEN — notification-service unavailable. " +
                        "Skipping new patient notification for patientId: {}",
                patientId);
        // Notification skipped — patient registration is NOT blocked
    }
}