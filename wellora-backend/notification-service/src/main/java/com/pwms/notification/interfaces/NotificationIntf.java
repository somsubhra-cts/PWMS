package com.pwms.notification.interfaces;

import com.pwms.notification.dto.*;
import com.pwms.notification.exception.NotificationNotFoundException;

import java.util.List;

public interface NotificationIntf {

    // ── Triggered by other modules ────────────────────────────

    // Patient module → new patient registered
    void notifyNewPatientRegistered(int patientId, int adminId);

    // Progress module → activity marked DONE
    void notifyActivityAppreciation(int patientId, int planId, String activityName);

    // Progress module → activity still PENDING
    void notifyActivityReminder(int patientId, int planId, String activityName);

    // Progress module → all activities DONE for 7-day cycle
    void notifyPlanCompleted(int patientId, int planId, int adminId);

    // Progress module → weekly cycle summary
    void notifyWeeklySummary(int patientId, int planId, double completionPct);

    // Report module → admin finalized report
    void notifyReportShared(int patientId, int planId);

    void notifyPlanAssigned(int patientId, int planId, String planName);

    // Wellness plan module → plan status → COMPLETED
    void notifyAppointmentReminder(int patientId, int planId);

    // Generic trigger — used internally for flexibility
    void sendNotification(NotificationRequestDTO request);

    // ── Read operations ───────────────────────────────────────

    List<NotificationDTO> getNotificationsForPatient(int patientId)
            throws NotificationNotFoundException;

    List<NotificationDTO> getUnreadForPatient(int patientId)
            throws NotificationNotFoundException;

    List<NotificationDTO> getNotificationsForAdmin(int adminId)
            throws NotificationNotFoundException;

    List<NotificationDTO> getUnreadForAdmin(int adminId)
            throws NotificationNotFoundException;

    // Mark a notification as read
    NotificationDTO markAsRead(int notificationId)
            throws NotificationNotFoundException;
}