package com.pwms.notification.controller;

import com.pwms.notification.interfaces.NotificationIntf;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// Internal endpoints — called by other services via HTTP
// Not exposed to frontend
@RestController
@RequestMapping("/api/notifications/internal")
@RequiredArgsConstructor
public class NotificationInternalController {

    private final NotificationIntf notificationService;

    // Called by patient-service when new patient registers
    @PostMapping("/plan-assigned")
    public ResponseEntity<Void> planAssigned(
            @RequestParam int patientId,
            @RequestParam int planId,
            @RequestParam String planName) {



        System.out.println("....................... CALLED..............................");
        notificationService.notifyPlanAssigned(patientId, planId, planName);
        System.out.println("....................... CALLED 2..............................");
        return ResponseEntity.ok().build();
    }

    @PostMapping("/new-patient")
    public ResponseEntity<Void> newPatient(
            @RequestParam int patientId,
            @RequestParam int adminId) {
        notificationService.notifyNewPatientRegistered(patientId, adminId);
        return ResponseEntity.ok().build();
    }

    // Called by progress-service when activity marked DONE
    @PostMapping("/activity-appreciation")
    public ResponseEntity<Void> activityAppreciation(
            @RequestParam int patientId,
            @RequestParam int planId,
            @RequestParam String activityName) {
        notificationService.notifyActivityAppreciation(patientId, planId, activityName);
        return ResponseEntity.ok().build();
    }

    // Called by progress-service when activity is PENDING
    @PostMapping("/activity-reminder")
    public ResponseEntity<Void> activityReminder(
            @RequestParam int patientId,
            @RequestParam int planId,
            @RequestParam String activityName) {
        notificationService.notifyActivityReminder(patientId, planId, activityName);
        return ResponseEntity.ok().build();
    }

    // Called by progress-service when all activities DONE
    @PostMapping("/plan-completed")
    public ResponseEntity<Void> planCompleted(
            @RequestParam int patientId,
            @RequestParam int planId,
            @RequestParam int adminId) {
        notificationService.notifyPlanCompleted(patientId, planId, adminId);
        return ResponseEntity.ok().build();
    }

    // Called by progress-service for weekly summary
    @PostMapping("/weekly-summary")
    public ResponseEntity<Void> weeklySummary(
            @RequestParam int patientId,
            @RequestParam int planId,
            @RequestParam double completionPct) {
        notificationService.notifyWeeklySummary(patientId, planId, completionPct);
        return ResponseEntity.ok().build();
    }

    // Called by report-service when report is published
    @PostMapping("/report-shared")
    public ResponseEntity<Void> reportShared(
            @RequestParam int patientId,
            @RequestParam int planId) {
        notificationService.notifyReportShared(patientId, planId);
        return ResponseEntity.ok().build();
    }

    // Called by wellnessplan-service or report-service when plan COMPLETED
    @PostMapping("/appointment-reminder")
    public ResponseEntity<Void> appointmentReminder(
            @RequestParam int patientId,
            @RequestParam int planId) {
        notificationService.notifyAppointmentReminder(patientId, planId);
        return ResponseEntity.ok().build();
    }
}