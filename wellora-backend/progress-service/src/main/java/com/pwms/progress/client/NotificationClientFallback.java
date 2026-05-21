package com.pwms.progress.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class NotificationClientFallback implements NotificationClient {

    @Override
    public void notifyActivityAppreciation(int patientId, int planId,
                                           String activityName) {
        log.warn("Fallback — notification-service unavailable. " +
                "ACTIVITY_APPRECIATION not sent for patientId: {}", patientId);
        throw new RuntimeException(
                "notification-service unavailable — ACTIVITY_APPRECIATION skipped for patientId: " + patientId);
    }

    @Override
    public void notifyActivityReminder(int patientId, int planId,
                                       String activityName) {
        log.warn("Fallback — notification-service unavailable. " +
                "ACTIVITY_REMINDER not sent for patientId: {}", patientId);
        throw new RuntimeException(
                "notification-service unavailable — ACTIVITY_REMINDER skipped for patientId: " + patientId);
    }

    @Override
    public void notifyPlanCompleted(int patientId, int planId, int adminId) {
        log.warn("Fallback — notification-service unavailable. " +
                "PLAN_COMPLETED not sent for patientId: {}", patientId);
        throw new RuntimeException(
                "notification-service unavailable — PLAN_COMPLETED skipped for patientId: " + patientId);
    }

    @Override
    public void notifyWeeklySummary(int patientId, int planId,
                                    double completionPct) {
        log.warn("Fallback — notification-service unavailable. " +
                "WEEKLY_SUMMARY not sent for patientId: {}", patientId);
        throw new RuntimeException(
                "notification-service unavailable — WEEKLY_SUMMARY skipped for patientId: " + patientId);
    }

    @Override
    public void notifyAppointmentReminder(int patientId, int planId) {
        log.warn("Fallback — notification-service unavailable. " +
                "APPOINTMENT_REMINDER not sent for patientId: {}", patientId);
        throw new RuntimeException(
                "notification-service unavailable — APPOINTMENT_REMINDER skipped for patientId: " + patientId);
    }
}

